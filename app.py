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
import tempfile
import shutil
from io import BytesIO

app = Flask(__name__)
CORS(app)

# Configuração de diretórios
DOWNLOAD_FOLDER = Path(__file__).parent / 'downloads'
DOWNLOAD_FOLDER.mkdir(exist_ok=True)

download_files = {}

# Tentar encontrar o FFmpeg instalado pelo winget
def find_ffmpeg():
    """Localiza o FFmpeg instalado no sistema ou usa yt-dlp bundled"""
    # Primeiro, verificar se ffmpeg está no PATH
    import shutil
    ffmpeg_path = shutil.which('ffmpeg')
    if ffmpeg_path:
        return os.path.dirname(ffmpeg_path)
    
    possible_paths = [
        r'C:\ffmpeg\ffmpeg-master-latest-win64-gpl\bin',  # Instalação manual
        r'C:\ProgramData\chocolatey\bin',
        r'C:\Program Files\FFmpeg\bin',
        r'C:\FFmpeg\bin',
        os.path.expanduser(r'~\scoop\apps\ffmpeg\current\bin'),
        r'C:\Program Files\BtbN\ffmpeg\bin',
        os.path.expanduser(r'~\AppData\Local\Microsoft\WinGet\Packages'),
    ]
    
    # Adicionar PATH do sistema
    system_path = os.environ.get('PATH', '')
    for path in system_path.split(os.pathsep):
        if 'ffmpeg' in path.lower():
            ffmpeg_exe = os.path.join(path, 'ffmpeg.exe')
            if os.path.exists(ffmpeg_exe):
                return path
    
    # Verificar paths conhecidos
    for path in possible_paths:
        if os.path.exists(path):
            # Buscar recursivamente apenas 2 níveis
            for root, dirs, files in os.walk(path):
                if 'ffmpeg.exe' in files:
                    return root
                if root.count(os.sep) - path.count(os.sep) >= 2:
                    del dirs[:]  # Não descer mais
    
    return None

# Configurar caminho do FFmpeg
FFMPEG_PATH = find_ffmpeg()
FFMPEG_LOCATION = None

if FFMPEG_PATH:
    print(f"✅ FFmpeg encontrado em: {FFMPEG_PATH}")
    FFMPEG_LOCATION = os.path.join(FFMPEG_PATH, 'ffmpeg.exe')
    os.environ['PATH'] = FFMPEG_PATH + os.pathsep + os.environ['PATH']
else:
    print("⚠️ FFmpeg não encontrado no sistema.")
    print("ℹ️  Tentando usar FFmpeg bundled do yt-dlp...")
    # yt-dlp pode usar seu próprio FFmpeg interno
    FFMPEG_LOCATION = None

# Armazenar status de downloads em memória
download_status = {}

# Histórico de downloads (persistente)
DOWNLOAD_HISTORY_FILE = DOWNLOAD_FOLDER / 'history.json'
download_history = []

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
    print(f"🔍 Buscando informações para: {url}")
    ydl_opts = {
        'quiet': False,
        'no_warnings': False,
        'extract_flat': 'in_playlist' if extract_playlist else False,
        'skip_download': True,
        'socket_timeout': 30,
        'noplaylist': not extract_playlist,
        'http_headers': {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'en-us,en;q=0.5',
            'Sec-Fetch-Mode': 'navigate',
        },
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
        return {
            'success': False,
            'error': str(e)
        }

def format_time(seconds):
    """Converte segundos para formato HH:MM:SS"""
    hours = int(seconds // 3600)
    minutes = int((seconds % 3600) // 60)
    secs = int(seconds % 60)
    return f"{hours:02d}:{minutes:02d}:{secs:02d}"

def download_video(url, video_info, selected_chapters, download_id, output_format='mp4', quality='best'):
    """Baixa o vídeo completo ou capítulos selecionados"""
    try:
        download_status[download_id]['status'] = 'downloading'
        download_status[download_id]['progress'] = 0
        
        video_title = sanitize_filename(video_info['title'])
        
        # Construir string de formato baseado na qualidade e formato
        format_string = build_format_string(output_format, quality)
        
        if not selected_chapters or len(selected_chapters) == len(video_info['chapters']):
            # Baixar vídeo completo
            output_path = DOWNLOAD_FOLDER / f"{video_title}.{output_format}"
            
            ydl_opts = {
                'format': format_string,
                'outtmpl': str(output_path),
                'progress_hooks': [lambda d: update_progress(d, download_id)],
                'merge_output_format': output_format if output_format in ['mp4', 'mkv', 'webm'] else 'mp4',
                'noplaylist': True,
                'http_headers': {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
                    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
                    'Accept-Language': 'en-us,en;q=0.5',
                    'Sec-Fetch-Mode': 'navigate',
                },
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
            
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
            
            download_status[download_id]['status'] = 'completed'
            download_status[download_id]['progress'] = 100
            download_status[download_id]['files'] = [str(output_path)]
        else:
            # Baixar vídeo completo primeiro e depois dividir em capítulos
            temp_video = DOWNLOAD_FOLDER / f"temp_{download_id}.mp4"
            
            ydl_opts = {
                'format': format_string,
                'outtmpl': str(temp_video),
                'progress_hooks': [lambda d: update_progress(d, download_id)],
                'merge_output_format': 'mp4',
                'noplaylist': True,
                'http_headers': {
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
                    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
                    'Accept-Language': 'en-us,en;q=0.5',
                    'Sec-Fetch-Mode': 'navigate',
                },
            }
            
            # Adicionar FFmpeg location se disponível
            if FFMPEG_LOCATION:
                ydl_opts['ffmpeg_location'] = FFMPEG_LOCATION
            
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
            
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
                    ffmpeg_cmd = os.path.join(FFMPEG_LOCATION, 'ffmpeg.exe')
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
        download_status[download_id]['error'] = str(e)

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
    
    # Gerar ID único para este download
    download_id = str(uuid.uuid4())
    
    download_status[download_id] = {
        'status': 'pending',
        'progress': 0,
        'files': []
    }
    
    # Iniciar download em thread separada
    thread = threading.Thread(
        target=download_video,
        args=(url, video_info, selected_chapters, download_id, output_format, quality)
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

if __name__ == '__main__':
    print("🚀 Iniciando YouTube Chapter Downloader...")
    print("📱 Acesse: http://localhost:5000")
    app.run(debug=True, host='0.0.0.0', port=5000)
