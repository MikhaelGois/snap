from flask import Flask, render_template, request, jsonify, send_file
from flask_cors import CORS
import yt_dlp
import os
import re
import uuid
from pathlib import Path
import threading
import time
import glob
import json
import tempfile
import shutil
from io import BytesIO
from urllib.parse import urlparse, parse_qsl, urlencode, urlunparse
import sys

APP_VERSION = '1.3.0'

try:
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    if hasattr(sys.stderr, 'reconfigure'):
        sys.stderr.reconfigure(encoding='utf-8', errors='replace')
except Exception:
    pass

try:
    import browser_cookie3
except Exception:
    browser_cookie3 = None

app = Flask(__name__)
CORS(app)

# Configuração de diretórios
DOWNLOAD_FOLDER = Path(__file__).parent / 'downloads'
DOWNLOAD_FOLDER.mkdir(exist_ok=True)

download_files = {}
GENERATED_COOKIES_FILE = DOWNLOAD_FOLDER / '_browser_cookies.txt'
_generated_cookies_mtime = 0

# Detectar e configurar FFmpeg automaticamente
def find_or_download_ffmpeg():
    """Localiza o FFmpeg instalado ou baixa automaticamente"""
    import platform
    import urllib.request
    import zipfile
    import tarfile
    
    # Diretório local para FFmpeg portátil
    local_ffmpeg_dir = Path(__file__).parent / 'ffmpeg_portable'
    
    # Nome do executável dependendo do OS
    ffmpeg_exe = 'ffmpeg.exe' if platform.system() == 'Windows' else 'ffmpeg'
    
    # 1. Verificar se já existe uma versão portátil no projeto
    if local_ffmpeg_dir.exists():
        for root, dirs, files in os.walk(local_ffmpeg_dir):
            if ffmpeg_exe in files:
                ffmpeg_path = os.path.join(root, ffmpeg_exe)
                print(f"✅ FFmpeg portátil encontrado: {ffmpeg_path}")
                return os.path.dirname(ffmpeg_path)
    
    # 2. Verificar se ffmpeg está no PATH do sistema
    ffmpeg_system = shutil.which('ffmpeg')
    if ffmpeg_system:
        print(f"✅ FFmpeg encontrado no PATH: {ffmpeg_system}")
        return os.path.dirname(ffmpeg_system)
    
    # 3. Verificar locais comuns de instalação (Windows)
    if platform.system() == 'Windows':
        possible_paths = [
            r'C:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin',
            r'C:\ProgramData\chocolatey\bin',
            r'C:\Program Files\FFmpeg\bin',
            r'C:\FFmpeg\bin',
            os.path.expanduser(r'~\scoop\apps\ffmpeg\current\bin'),
        ]
        
        for path in possible_paths:
            if os.path.exists(os.path.join(path, ffmpeg_exe)):
                print(f"✅ FFmpeg encontrado: {path}")
                return path
    
    # 4. Baixar FFmpeg automaticamente
    print("⏬ FFmpeg não encontrado. Baixando versão portátil...")
    try:
        local_ffmpeg_dir.mkdir(exist_ok=True)
        
        if platform.system() == 'Windows':
            # Download FFmpeg para Windows
            url = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip"
            zip_path = local_ffmpeg_dir / 'ffmpeg.zip'
            
            print(f"  Baixando de {url}...")
            urllib.request.urlretrieve(url, zip_path)
            
            print("  Extraindo...")
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                zip_ref.extractall(local_ffmpeg_dir)
            
            zip_path.unlink()
            
            # Encontrar o executável extraído
            for root, dirs, files in os.walk(local_ffmpeg_dir):
                if ffmpeg_exe in files:
                    ffmpeg_path = os.path.join(root, ffmpeg_exe)
                    print(f"✅ FFmpeg instalado com sucesso: {ffmpeg_path}")
                    return os.path.dirname(ffmpeg_path)
        
        elif platform.system() == 'Linux':
            # Para Linux, baixar build estático
            url = "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz"
            tar_path = local_ffmpeg_dir / 'ffmpeg.tar.xz'
            
            print(f"  Baixando de {url}...")
            urllib.request.urlretrieve(url, tar_path)
            
            print("  Extraindo...")
            with tarfile.open(tar_path, 'r:xz') as tar_ref:
                tar_ref.extractall(local_ffmpeg_dir)
            
            tar_path.unlink()
            
            # Encontrar e dar permissão de execução
            for root, dirs, files in os.walk(local_ffmpeg_dir):
                if ffmpeg_exe in files:
                    ffmpeg_path = os.path.join(root, ffmpeg_exe)
                    os.chmod(ffmpeg_path, 0o755)
                    print(f"✅ FFmpeg instalado com sucesso: {ffmpeg_path}")
                    return os.path.dirname(ffmpeg_path)
        
        elif platform.system() == 'Darwin':  # macOS
            print("⚠️ macOS detectado. Por favor, instale FFmpeg usando:")
            print("   brew install ffmpeg")
            return None
            
    except Exception as e:
        print(f"❌ Erro ao baixar FFmpeg: {e}")
        print("⚠️ Alguns recursos podem não funcionar sem FFmpeg.")
        return None
    
    return None

# Configurar caminho do FFmpeg
FFMPEG_PATH = find_or_download_ffmpeg()
FFMPEG_LOCATION = None

if FFMPEG_PATH:
    ffmpeg_exe = 'ffmpeg.exe' if os.name == 'nt' else 'ffmpeg'
    FFMPEG_LOCATION = os.path.join(FFMPEG_PATH, ffmpeg_exe)
    os.environ['PATH'] = FFMPEG_PATH + os.pathsep + os.environ['PATH']
    print(f"🎬 FFmpeg configurado: {FFMPEG_LOCATION}")
else:
    print("⚠️ FFmpeg não disponível. Funcionalidades de corte de capítulos serão limitadas.")
    FFMPEG_LOCATION = None

# Armazenar status de downloads em memória
download_status = {}

# Histórico de downloads (persistente)
DOWNLOAD_HISTORY_FILE = DOWNLOAD_FOLDER / 'history.json'
download_history = []

def get_cookie_file_path():
    """Retorna caminho padrão do cookies.txt do projeto"""
    return Path(__file__).parent / 'cookies.txt'

def load_download_history():
    """Carrega histórico de downloads"""
    global download_history
    try:
        if DOWNLOAD_HISTORY_FILE.exists():
            with open(DOWNLOAD_HISTORY_FILE, 'r', encoding='utf-8') as f:
                download_history = json.load(f)
    except:
        download_history = []

def save_download_history():
    """Salva histórico de downloads"""
    try:
        with open(DOWNLOAD_HISTORY_FILE, 'w', encoding='utf-8') as f:
            json.dump(download_history[-100:], f, ensure_ascii=False, indent=2)  # Últimos 100
    except Exception as e:
        print(f"Erro ao salvar histórico: {e}")

load_download_history()

def sanitize_filename(filename):
    """Remove caracteres inválidos do nome do arquivo"""
    return re.sub(r'[<>:"/\\|?*]', '', filename)

ANSI_ESCAPE_RE = re.compile(r'\x1B\[[0-?]*[ -/]*[@-~]')

def strip_ansi(text: str) -> str:
    """Remove códigos ANSI (cores) de mensagens de erro"""
    return ANSI_ESCAPE_RE.sub('', text or '')

def is_youtube_url(url: str) -> bool:
    """Verifica se a URL pertence ao YouTube"""
    try:
        host = (urlparse(url).hostname or '').lower()
        return 'youtube.com' in host or 'youtu.be' in host
    except Exception:
        return False

def normalize_video_url(url: str, extract_playlist: bool = False) -> str:
    """Normaliza URL do YouTube para evitar contexto de playlist em modo single"""
    if not url or extract_playlist or not is_youtube_url(url):
        return url

    try:
        parsed = urlparse(url)
        filtered_query = [
            (k, v)
            for (k, v) in parse_qsl(parsed.query, keep_blank_values=True)
            if k.lower() not in {'list', 'index', 'pp', 'start_radio'}
        ]
        return urlunparse(parsed._replace(query=urlencode(filtered_query, doseq=True)))
    except Exception:
        return url

def parse_cookiesfrombrowser(value: str):
    """Converte string de cookiesfrombrowser para formato aceito pelo yt-dlp"""
    if not value:
        return None
    parts = [part.strip() for part in value.split(':') if part.strip()]
    if not parts:
        return None
    return tuple(parts)

def get_browser_cookie_candidates():
    """Lista navegadores para tentativa automática de extração de cookies"""
    candidates = []

    env_browser = os.environ.get('YTDLP_COOKIES_FROM_BROWSER')
    if env_browser:
        candidates.append(env_browser.strip())

    # Ordem priorizada para Windows
    candidates.extend(['edge', 'chrome', 'firefox', 'brave', 'opera', 'vivaldi'])

    # Remove duplicados preservando ordem
    unique = []
    seen = set()
    for browser in candidates:
        key = (browser or '').lower()
        if key and key not in seen:
            seen.add(key)
            unique.append(browser)

    return unique

def is_auth_or_bot_error(error_text: str) -> bool:
    """Detecta erros comuns de bloqueio/autenticação do YouTube"""
    text = (error_text or '').lower()
    patterns = [
        'sign in to confirm you\'re not a bot',
        'use --cookies-from-browser',
        'use --cookies',
        'how-do-i-pass-cookies-to-yt-dlp',
        'this video is unavailable',
        'http error 429',
    ]
    return any(p in text for p in patterns)

def build_auth_help_message(error_text: str) -> str:
    """Mensagem amigável para erros de autenticação no YouTube"""
    cleaned = strip_ansi(error_text)
    return (
        "YouTube bloqueou esta requisição e pediu verificação anti-bot. "
        "O app já tentou usar cookies do navegador automaticamente, mas não conseguiu. "
        "Feche o YouTube no navegador, tente novamente e, se persistir, configure um cookies.txt "
        "na raiz do projeto ou defina YTDLP_COOKIES_FROM_BROWSER (ex.: edge ou chrome). "
        f"\n\nDetalhe técnico: {cleaned}"
    )

def try_extract_with_browser_cookies(url, base_opts):
    """Tenta extrair informações usando cookies do navegador"""
    last_error = None
    for browser in get_browser_cookie_candidates():
        browser_tuple = parse_cookiesfrombrowser(browser)
        if not browser_tuple:
            continue

        retry_opts = {
            **base_opts,
            'cookiesfrombrowser': browser_tuple,
        }

        try:
            print(f"🔐 Tentando extração com cookies do navegador: {browser}")
            with yt_dlp.YoutubeDL(retry_opts) as ydl:
                info = ydl.extract_info(url, download=False)
                return info, None
        except Exception as e:
            last_error = e

    return None, last_error

def try_download_with_browser_cookies(url, base_opts):
    """Tenta baixar usando cookies do navegador"""
    last_error = None
    for browser in get_browser_cookie_candidates():
        browser_tuple = parse_cookiesfrombrowser(browser)
        if not browser_tuple:
            continue

        retry_opts = {
            **base_opts,
            'cookiesfrombrowser': browser_tuple,
        }

        try:
            print(f"🔐 Tentando download com cookies do navegador: {browser}")
            with yt_dlp.YoutubeDL(retry_opts) as ydl:
                ydl.download([url])
            return True, None
        except Exception as e:
            last_error = e

    return False, last_error

def resolve_cookie_file():
    """Resolve arquivo de cookies para yt-dlp, se existir"""
    env_cookie = os.environ.get('YTDLP_COOKIES_FILE')
    if env_cookie and os.path.exists(env_cookie):
        return env_cookie

    local_cookie = get_cookie_file_path()
    if local_cookie.exists():
        return str(local_cookie)

    return None

def _to_netscape_cookie_line(cookie) -> str:
    """Converte cookie para formato Netscape compatível com yt-dlp"""
    domain = cookie.domain or ''
    include_subdomains = 'TRUE' if domain.startswith('.') else 'FALSE'
    path = cookie.path or '/'
    secure = 'TRUE' if cookie.secure else 'FALSE'
    expires = str(int(cookie.expires or 0))
    name = cookie.name or ''
    value = cookie.value or ''
    return f"{domain}\t{include_subdomains}\t{path}\t{secure}\t{expires}\t{name}\t{value}"

def export_browser_cookies_file(force: bool = False):
    """Exporta cookies do navegador para arquivo Netscape (fallback do YouTube)"""
    global _generated_cookies_mtime

    if os.environ.get('YTDLP_DISABLE_BROWSER_COOKIE_EXPORT', '').lower() in {'1', 'true', 'yes'}:
        return None

    if not browser_cookie3:
        return None

    try:
        if GENERATED_COOKIES_FILE.exists() and not force:
            # Reusa por 15 minutos
            if (time.time() - GENERATED_COOKIES_FILE.stat().st_mtime) < 900:
                return str(GENERATED_COOKIES_FILE)
    except Exception:
        pass

    browser_loaders = {
        'edge': getattr(browser_cookie3, 'edge', None),
        'chrome': getattr(browser_cookie3, 'chrome', None),
        'chromium': getattr(browser_cookie3, 'chromium', None),
        'firefox': getattr(browser_cookie3, 'firefox', None),
        'brave': getattr(browser_cookie3, 'brave', None),
        'opera': getattr(browser_cookie3, 'opera', None),
        'vivaldi': getattr(browser_cookie3, 'vivaldi', None),
    }

    allowed_domains = (
        '.youtube.com', 'youtube.com', '.google.com', 'google.com', '.youtu.be', 'youtu.be'
    )

    for browser in get_browser_cookie_candidates():
        key = (browser or '').split(':', 1)[0].strip().lower()
        loader = browser_loaders.get(key)
        if not loader:
            continue

        try:
            cookiejar = loader()
            lines = ['# Netscape HTTP Cookie File']

            for cookie in cookiejar:
                domain = (cookie.domain or '').lower()
                if any(domain.endswith(d) for d in allowed_domains):
                    lines.append(_to_netscape_cookie_line(cookie))

            if len(lines) <= 1:
                continue

            with open(GENERATED_COOKIES_FILE, 'w', encoding='utf-8', newline='\n') as f:
                f.write('\n'.join(lines) + '\n')

            _generated_cookies_mtime = time.time()
            print(f"🍪 Cookies exportados de {browser} para: {GENERATED_COOKIES_FILE}")
            return str(GENERATED_COOKIES_FILE)
        except Exception as e:
            print(f"⚠️ Falha ao exportar cookies de {browser}: {e}")

    return None

def build_ydl_base_opts():
    """Opções base do yt-dlp (headers, cookies, etc.)"""
    opts = {
        'socket_timeout': 30,
        'geo_bypass': True,
        'retries': 5,
        'fragment_retries': 5,
        'concurrent_fragment_downloads': 1,
        'extractor_args': {
            'youtube': {
                'player_client': ['android', 'web']
            }
        },
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'en-us,en;q=0.5',
            'Sec-Fetch-Mode': 'navigate',
            'Referer': 'https://www.youtube.com/',
            'Origin': 'https://www.youtube.com',
        },
    }

    cookie_file = resolve_cookie_file()
    if not cookie_file:
        cookie_file = export_browser_cookies_file()
    if cookie_file:
        opts['cookiefile'] = cookie_file

    cookies_from_browser = os.environ.get('YTDLP_COOKIES_FROM_BROWSER')
    if cookies_from_browser:
        parsed = parse_cookiesfrombrowser(cookies_from_browser)
        if parsed:
            opts['cookiesfrombrowser'] = parsed

    impersonate = os.environ.get('YTDLP_IMPERSONATE')
    if impersonate:
        opts['impersonate'] = impersonate

    return opts

def fallback_format_for(output_format):
    """Formato de fallback para tentativas alternativas"""
    if output_format in ['mp3', 'm4a', 'wav', 'opus']:
        return 'bestaudio[ext=m4a]/bestaudio/best'
    if output_format == 'mp4':
        return 'best[ext=mp4]/best'
    return 'best'

def try_download_with_fallback(url, ydl_opts, output_format, download_id=None):
    """Tenta download e faz fallback em caso de 403"""
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        return
    except Exception as e:
        error_text = str(e)

        if is_youtube_url(url) and is_auth_or_bot_error(error_text):
            ok, last_error = try_download_with_browser_cookies(url, ydl_opts)
            if ok:
                return
            if last_error:
                error_text = str(last_error)

        if 'HTTP Error 403' not in error_text:
            raise

        print("⚠️ 403 detectado. Tentando fallback de formato e cliente...")
        fallback_opts = {
            **ydl_opts,
            'format': fallback_format_for(output_format),
            'extractor_args': {
                'youtube': {
                    'player_client': ['android', 'web', 'tv_embedded']
                }
            },
        }

        try:
            with yt_dlp.YoutubeDL(fallback_opts) as ydl:
                ydl.download([url])
        except Exception as fallback_error:
            fallback_error_text = str(fallback_error)
            if is_youtube_url(url) and is_auth_or_bot_error(fallback_error_text):
                ok, last_error = try_download_with_browser_cookies(url, fallback_opts)
                if ok:
                    return
                if last_error:
                    raise last_error
            raise

def build_format_string(output_format, quality):
    """Constrói a string de formato para yt-dlp baseado no formato e qualidade"""
    # Formatos de áudio
    if output_format in ['mp3', 'm4a', 'wav', 'opus']:
        return 'bestaudio/best'
    
    # Formatos de vídeo
    if quality == 'best':
        if output_format == 'mp4':
            return 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best'
        elif output_format == 'mkv':
            return 'bestvideo+bestaudio/best'
        elif output_format == 'webm':
            return 'bestvideo[ext=webm]+bestaudio[ext=webm]/best[ext=webm]/best'
        else:  # avi ou outros
            return 'bestvideo+bestaudio/best'
    else:
        # Qualidade específica
        height = quality
        if output_format == 'mp4':
            return f'bestvideo[height<={height}][ext=mp4]+bestaudio[ext=m4a]/best[height<={height}][ext=mp4]/best[height<={height}]'
        elif output_format == 'mkv':
            return f'bestvideo[height<={height}]+bestaudio/best[height<={height}]'
        elif output_format == 'webm':
            return f'bestvideo[height<={height}][ext=webm]+bestaudio[ext=webm]/best[height<={height}][ext=webm]/best[height<={height}]'
        else:
            return f'bestvideo[height<={height}]+bestaudio/best[height<={height}]'

def get_video_info(url, extract_playlist=False):
    """Extrai informações do vídeo/playlist incluindo capítulos"""
    url = normalize_video_url(url, extract_playlist=extract_playlist)
    print(f"🔍 Buscando informações para: {url}")
    ydl_opts = {
        **build_ydl_base_opts(),
        'quiet': False,
        'no_warnings': False,
        'extract_flat': 'in_playlist' if extract_playlist else False,
        'skip_download': True,
        'noplaylist': not extract_playlist,
    }
    
    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            print("⏳ Extraindo informações do vídeo...")
            info = ydl.extract_info(url, download=False)
            print("✅ Informações extraídas com sucesso!")
            
            chapters = []
            if info.get('chapters'):
                for idx, chapter in enumerate(info['chapters']):
                    chapters.append({
                        'id': idx,
                        'title': chapter.get('title', f'Capítulo {idx + 1}'),
                        'start_time': chapter.get('start_time', 0),
                        'end_time': chapter.get('end_time', info.get('duration', 0))
                    })
            
            # Detectar se é playlist
            is_playlist = info.get('_type') == 'playlist'
            playlist_count = len(info.get('entries', [])) if is_playlist else 0
            
            return {
                'success': True,
                'title': info.get('title', 'Unknown'),
                'duration': info.get('duration', 0),
                'thumbnail': info.get('thumbnail', ''),
                'chapters': chapters,
                'has_chapters': len(chapters) > 0,
                'is_playlist': is_playlist,
                'playlist_count': playlist_count,
                'extractor': info.get('extractor', 'youtube')
            }
    except Exception as e:
        error_text = str(e)

        if is_youtube_url(url) and is_auth_or_bot_error(error_text):
            info, retry_error = try_extract_with_browser_cookies(url, ydl_opts)
            if info:
                chapters = []
                if info.get('chapters'):
                    for idx, chapter in enumerate(info['chapters']):
                        chapters.append({
                            'id': idx,
                            'title': chapter.get('title', f'Capítulo {idx + 1}'),
                            'start_time': chapter.get('start_time', 0),
                            'end_time': chapter.get('end_time', info.get('duration', 0))
                        })

                is_playlist = info.get('_type') == 'playlist'
                playlist_count = len(info.get('entries', [])) if is_playlist else 0

                return {
                    'success': True,
                    'title': info.get('title', 'Unknown'),
                    'duration': info.get('duration', 0),
                    'thumbnail': info.get('thumbnail', ''),
                    'chapters': chapters,
                    'has_chapters': len(chapters) > 0,
                    'is_playlist': is_playlist,
                    'playlist_count': playlist_count,
                    'extractor': info.get('extractor', 'youtube')
                }

            return {
                'success': False,
                'error': build_auth_help_message(error_text)
            }

        return {
            'success': False,
            'error': strip_ansi(error_text)
        }

def format_time(seconds):
    """Converte segundos para formato HH:MM:SS"""
    hours = int(seconds // 3600)
    minutes = int((seconds % 3600) // 60)
    secs = int(seconds % 60)
    return f"{hours:02d}:{minutes:02d}:{secs:02d}"

def download_video(url, video_info, selected_chapters, download_id, output_format='mp4', quality='best', download_type='chapters'):
    """Baixa o vídeo completo ou capítulos selecionados
    
    Args:
        download_type: 'single' para arquivo único, 'chapters' para separar em capítulos
    """
    try:
        download_status[download_id]['status'] = 'downloading'
        download_status[download_id]['progress'] = 0
        
        video_title = sanitize_filename(video_info['title'])
        
        # Construir string de formato baseado na qualidade e formato
        format_string = build_format_string(output_format, quality)
        
        # Decidir se baixa como arquivo único ou separado
        should_download_single = (
            download_type == 'single' or 
            not selected_chapters or 
            not video_info.get('has_chapters') or
            len(selected_chapters) == 0
        )
        
        if should_download_single:
            # Baixar vídeo completo como arquivo único
            output_path = DOWNLOAD_FOLDER / f"{video_title}.{output_format}"
            
            ydl_opts = {
                **build_ydl_base_opts(),
                'format': format_string,
                'outtmpl': str(output_path),
                'progress_hooks': [lambda d: update_progress(d, download_id)],
                'merge_output_format': output_format if output_format in ['mp4', 'mkv', 'webm'] else 'mp4',
                'noplaylist': True,
            }
            
            # Adicionar FFmpeg location se disponível
            if FFMPEG_LOCATION:
                ydl_opts['ffmpeg_location'] = FFMPEG_LOCATION
            
            # Adicionar postprocessor para áudio
            if output_format in ['mp3', 'm4a', 'wav', 'opus']:
                ydl_opts['postprocessors'] = [{
                    'key': 'FFmpegExtractAudio',
                    'preferredcodec': output_format,
                    'preferredquality': '192' if output_format == 'mp3' else '0',
                }]
            
            try_download_with_fallback(url, ydl_opts, output_format, download_id)
            
            download_status[download_id]['status'] = 'completed'
            download_status[download_id]['progress'] = 100
            download_status[download_id]['files'] = [str(output_path)]
        else:
            # Baixar vídeo completo primeiro e depois dividir em capítulos
            temp_video = DOWNLOAD_FOLDER / f"temp_{download_id}.mp4"
            
            ydl_opts = {
                **build_ydl_base_opts(),
                'format': format_string,
                'outtmpl': str(temp_video),
                'progress_hooks': [lambda d: update_progress(d, download_id)],
                'merge_output_format': 'mp4',
                'noplaylist': True,
            }
            
            # Adicionar FFmpeg location se disponível
            if FFMPEG_LOCATION:
                ydl_opts['ffmpeg_location'] = FFMPEG_LOCATION
            
            try_download_with_fallback(url, ydl_opts, output_format, download_id)
            
            # Dividir em capítulos selecionados
            download_status[download_id]['status'] = 'splitting'
            output_files = []
            
            for chapter_id in selected_chapters:
                chapter = video_info['chapters'][chapter_id]
                chapter_title = sanitize_filename(chapter['title'])
                output_file = DOWNLOAD_FOLDER / f"{video_title} - {chapter_title}.{output_format}"
                
                start_time = format_time(chapter['start_time'])
                duration = chapter['end_time'] - chapter['start_time']
                
                # Usar FFmpeg para extrair o capítulo
                import subprocess
                
                # Caminho do FFmpeg
                if FFMPEG_LOCATION:
                    ffmpeg_cmd = FFMPEG_LOCATION
                else:
                    ffmpeg_cmd = 'ffmpeg'
                
                # Se for áudio apenas, extrair áudio
                if output_format in ['mp3', 'm4a', 'wav', 'opus']:
                    cmd = [
                        ffmpeg_cmd,
                        '-i', str(temp_video),
                        '-ss', start_time,
                        '-t', str(duration),
                        '-vn',  # Sem vídeo
                        '-acodec', 'libmp3lame' if output_format == 'mp3' else 'copy',
                        '-y',
                        str(output_file)
                    ]
                else:
                    cmd = [
                        ffmpeg_cmd,
                        '-i', str(temp_video),
                        '-ss', start_time,
                        '-t', str(duration),
                        '-c', 'copy',
                        '-y',
                        str(output_file)
                    ]
                
                subprocess.run(cmd, capture_output=True, check=True)
                output_files.append(str(output_file))
            
            # Remover arquivo temporário
            if temp_video.exists():
                temp_video.unlink()
            
            download_status[download_id]['status'] = 'completed'
            download_status[download_id]['progress'] = 100
            download_status[download_id]['files'] = output_files
        
        # Adicionar ao histórico
        download_history.append({
            'id': download_id,
            'title': video_info.get('title', 'Unknown'),
            'format': output_format,
            'quality': quality,
            'files': download_status[download_id]['files'],
            'timestamp': time.strftime('%Y-%m-%d %H:%M:%S'),
            'url': url
        })
        save_download_history()
            
    except Exception as e:
        download_status[download_id]['status'] = 'error'
        download_status[download_id]['error'] = strip_ansi(str(e))

def update_progress(d, download_id):
    """Atualiza o progresso do download"""
    if d['status'] == 'downloading':
        if d.get('total_bytes'):
            progress = (d.get('downloaded_bytes', 0) / d['total_bytes']) * 100
            download_status[download_id]['progress'] = int(progress)
        elif d.get('total_bytes_estimate'):
            progress = (d.get('downloaded_bytes', 0) / d['total_bytes_estimate']) * 100
            download_status[download_id]['progress'] = int(progress)

@app.route('/')
def index():
    """Página principal"""
    return render_template('index.html')

@app.route('/api/video-info', methods=['POST'])
def video_info():
    """Endpoint para obter informações do vídeo"""
    print("📨 Recebendo requisição /api/video-info")
    data = request.json
    url = data.get('url')
    
    print(f"🔗 URL recebida: {url}")
    
    if not url:
        return jsonify({'success': False, 'error': 'URL não fornecida'}), 400
    
    url = normalize_video_url(url, extract_playlist=False)
    info = get_video_info(url)
    print(f"📤 Retornando resposta: {info.get('success', False)}")
    return jsonify(info)

@app.route('/api/download', methods=['POST'])
def start_download():
    """Inicia o download do vídeo ou capítulos selecionados"""
    data = request.json
    url = data.get('url')
    video_info = data.get('video_info')
    selected_chapters = data.get('selected_chapters', [])
    output_format = data.get('format', 'mp4')
    quality = data.get('quality', 'best')
    
    if not url or not video_info:
        return jsonify({'success': False, 'error': 'Dados incompletos'}), 400

    url = normalize_video_url(url, extract_playlist=False)
    
    # Gerar ID único para este download
    download_id = str(uuid.uuid4())
    
    download_status[download_id] = {
        'status': 'pending',
        'progress': 0,
        'files': []
    }
    
    # Obter tipo de download (single ou chapters)
    download_type = data.get('download_type', 'chapters')
    
    # Iniciar download em thread separada
    thread = threading.Thread(
        target=download_video,
        args=(url, video_info, selected_chapters, download_id, output_format, quality, download_type)
    )
    thread.start()
    
    return jsonify({
        'success': True,
        'download_id': download_id
    })

@app.route('/api/download-status/<download_id>')
def get_download_status(download_id):
    """Retorna o status de um download"""
    status = download_status.get(download_id)
    
    if not status:
        return jsonify({'success': False, 'error': 'Download não encontrado'}), 404
    
    return jsonify({
        'success': True,
        'status': status['status'],
        'progress': status['progress'],
        'error': status.get('error'),
        'files': status.get('files', [])
    })

@app.route('/api/download-file/<path:filename>')
def download_file(filename):
    """Serve arquivo para download"""
    try:
        file_path = DOWNLOAD_FOLDER / filename
        
        if not file_path.exists():
            return jsonify({'success': False, 'error': 'Arquivo não encontrado'}), 404
        
        return send_file(
            file_path,
            as_attachment=True,
            download_name=filename
        )
        
        thread = threading.Thread(target=delete_file_delayed, daemon=True)
        thread.start()
        
        return response
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/api/history')
def get_history():
    """Retorna histórico de downloads"""
    return jsonify({
        'success': True,
        'history': download_history[-50:]  # Últimos 50
    })

@app.route('/api/history/clear', methods=['POST'])
def clear_history():
    """Limpa histórico de downloads"""
    global download_history
    download_history = []
    save_download_history()
    return jsonify({'success': True})

@app.route('/api/cookies/status')
def cookie_status():
    """Retorna status do arquivo cookies.txt"""
    cookie_path = get_cookie_file_path()
    exists = cookie_path.exists()
    size = cookie_path.stat().st_size if exists else 0

    return jsonify({
        'success': True,
        'exists': exists,
        'size': size,
        'path': str(cookie_path) if exists else str(cookie_path),
    })

@app.route('/api/cookies/upload', methods=['POST'])
def cookie_upload():
    """Upload de cookies.txt para autenticação do YouTube"""
    if 'cookie_file' not in request.files:
        return jsonify({'success': False, 'error': 'Arquivo não enviado'}), 400

    file = request.files['cookie_file']
    if not file or not file.filename:
        return jsonify({'success': False, 'error': 'Arquivo inválido'}), 400

    filename = file.filename.lower()
    if not filename.endswith('.txt'):
        return jsonify({'success': False, 'error': 'Envie um arquivo .txt (formato Netscape)'}), 400

    try:
        content = file.read().decode('utf-8', errors='ignore')
        if 'youtube.com' not in content and 'youtu.be' not in content:
            return jsonify({'success': False, 'error': 'cookies.txt sem entradas do YouTube'}), 400

        cookie_path = get_cookie_file_path()
        with open(cookie_path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)

        return jsonify({
            'success': True,
            'message': 'cookies.txt salvo com sucesso',
            'path': str(cookie_path),
            'size': cookie_path.stat().st_size,
        })
    except Exception as e:
        return jsonify({'success': False, 'error': f'Falha ao salvar cookies: {e}'}), 500

@app.route('/api/cookies/clear', methods=['POST'])
def cookie_clear():
    """Remove cookies.txt local"""
    try:
        cookie_path = get_cookie_file_path()
        if cookie_path.exists():
            cookie_path.unlink()
        return jsonify({'success': True})
    except Exception as e:
        return jsonify({'success': False, 'error': f'Falha ao remover cookies: {e}'}), 500

if __name__ == '__main__':
    print(f"🚀 Iniciando YouTube Chapter Downloader v{APP_VERSION}...")
    print("📱 Acesse: http://localhost:5000")
    run_kwargs = {
        'debug': True,
        'host': '0.0.0.0',
        'port': 5000,
        'use_reloader': True,
    }

    # Evita reinicializações em loop por alterações no ambiente virtual/site-packages.
    # Isso é comum no Windows quando watchdog observa arquivos dentro de .venv.
    reloader_excludes = [
        '*/.venv/*',
        '*\\.venv\\*',
        '*/site-packages/*',
        '*\\site-packages\\*',
        '*/__pycache__/*',
        '*\\__pycache__\\*',
    ]

    try:
        app.run(**run_kwargs, exclude_patterns=reloader_excludes)
    except TypeError:
        print("ℹ️ Versão do Werkzeug sem suporte a exclude_patterns; iniciando sem auto-reload.")
        app.run(**{**run_kwargs, 'use_reloader': False})
