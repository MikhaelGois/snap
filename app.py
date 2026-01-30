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
import tempfile
import shutil
from io import BytesIO

app = Flask(__name__)
CORS(app)

# Configuração de diretórios
TEMP_FOLDER = Path(tempfile.gettempdir()) / 'youtube_downloader'
TEMP_FOLDER.mkdir(exist_ok=True)

# Remover DOWNLOAD_FOLDER (agora usando temp)
download_files = {}

# Tentar encontrar o FFmpeg instalado pelo winget
def find_ffmpeg():
    """Localiza o FFmpeg instalado no sistema"""
    possible_paths = [
        r'C:\ProgramData\chocolatey\bin',
        r'C:\Program Files\FFmpeg\bin',
        r'C:\FFmpeg\bin',
        os.path.expanduser(r'~\scoop\apps\ffmpeg\current\bin'),
        r'C:\Program Files\BtbN\ffmpeg\bin',  # Outro path comum
    ]
    
    # Adicionar PATH do sistema
    system_path = os.environ.get('PATH', '')
    for path in system_path.split(os.pathsep):
        if 'ffmpeg' in path.lower() and os.path.exists(os.path.join(path, 'ffmpeg.exe')):
            return path
    
    # Verificar paths conhecidos
    for path in possible_paths:
        ffmpeg_exe = os.path.join(path, 'ffmpeg.exe')
        if os.path.exists(ffmpeg_exe):
            return path
    
    return None

# Configurar caminho do FFmpeg
FFMPEG_PATH = find_ffmpeg()
if FFMPEG_PATH:
    print(f"✅ FFmpeg encontrado em: {FFMPEG_PATH}")
    os.environ['PATH'] = FFMPEG_PATH + os.pathsep + os.environ['PATH']
else:
    print("⚠️ FFmpeg não encontrado. Algumas funcionalidades podem não funcionar.")

# Armazenar status de downloads em memória
download_status = {}

def cleanup_old_files():
    """Limpa arquivos temporários antigos (mais de 1 hora)"""
    try:
        import datetime
        now = time.time()
        for filepath in TEMP_FOLDER.glob('*'):
            if os.path.isfile(filepath):
                if now - os.path.getctime(filepath) > 3600:  # 1 hora
                    try:
                        os.remove(filepath)
                    except:
                        pass
    except:
        pass

# Limpar arquivos ao iniciar
cleanup_old_files()

def get_default_headers():
    """Retorna headers padrão para requisições"""
    return {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7',
        'Accept-Language': 'pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7',
        'Accept-Encoding': 'gzip, deflate, br',
        'DNT': '1',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'none',
        'Sec-Fetch-User': '?1',
        'Cache-Control': 'max-age=0',
    }

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

def get_video_info(url):
    """Extrai informações do vídeo incluindo capítulos"""
    print(f"🔍 Buscando informações para: {url}")
    ydl_opts = {
        'quiet': False,
        'no_warnings': False,
        'extract_flat': False,
        'skip_download': True,
        'socket_timeout': 30,
        'noplaylist': True,
        'extractor_args': {
            'youtube': {
                'skip_unavailable_videos': True,
                'player_client': ['web', 'mweb'],  # Tentar múltiplos clientes
            }
        },
        'http_headers': get_default_headers(),
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
            
            return {
                'success': True,
                'title': info.get('title', 'Unknown'),
                'duration': info.get('duration', 0),
                'thumbnail': info.get('thumbnail', ''),
                'chapters': chapters,
                'has_chapters': len(chapters) > 0
            }
    except Exception as e:
        error_msg = str(e)
        print(f"❌ Erro ao extrair vídeo: {error_msg}")
        
        # Se o erro for sobre player response, tenta com configuração alternativa
        if "player response" in error_msg.lower():
            print("🔄 Tentando com configuração alternativa...")
            try:
                ydl_opts_alt = ydl_opts.copy()
                ydl_opts_alt['extractor_args'] = {
                    'youtube': {
                        'skip_unavailable_videos': True,
                        'player_client': ['android', 'web'],
                    }
                }
                with yt_dlp.YoutubeDL(ydl_opts_alt) as ydl:
                    info = ydl.extract_info(url, download=False)
                    
                    chapters = []
                    if info.get('chapters'):
                        for idx, chapter in enumerate(info['chapters']):
                            chapters.append({
                                'id': idx,
                                'title': chapter.get('title', f'Capítulo {idx + 1}'),
                                'start_time': chapter.get('start_time', 0),
                                'end_time': chapter.get('end_time', info.get('duration', 0))
                            })
                    
                    return {
                        'success': True,
                        'title': info.get('title', 'Unknown'),
                        'duration': info.get('duration', 0),
                        'thumbnail': info.get('thumbnail', ''),
                        'chapters': chapters,
                        'has_chapters': len(chapters) > 0
                    }
            except Exception as e2:
                print(f"❌ Também falhou com alternativa: {e2}")
        
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

def download_video(url, video_info, selected_chapters, download_id, output_format='mp4', quality='best', download_type='chapters'):
    """Baixa o vídeo completo ou capítulos selecionados"""
    try:
        download_status[download_id]['status'] = 'downloading'
        download_status[download_id]['progress'] = 0
        
        video_title = sanitize_filename(video_info['title'])
        
        # Construir string de formato baseado na qualidade e formato
        format_string = build_format_string(output_format, quality)
        
        # Usar download_type para determinar se faz splitting mesmo com todos os capítulos selecionados
        should_split = (download_type == 'chapters' and 
                       selected_chapters and 
                       len(selected_chapters) > 0 and
                       video_info.get('has_chapters', False))
        
        if not should_split or not selected_chapters or len(selected_chapters) == len(video_info['chapters']):
            if download_type == 'complete' or not should_split:
                # Baixar vídeo completo
                output_path = TEMP_FOLDER / f"{video_title}.{output_format}"
                
                ydl_opts = {
                    'format': format_string,
                    'outtmpl': str(output_path),
                    'progress_hooks': [lambda d: update_progress(d, download_id)],
                    'merge_output_format': output_format if output_format in ['mp4', 'mkv', 'webm'] else 'mp4',
                    'noplaylist': True,
                    'http_headers': get_default_headers(),
                'postprocessors': [] if output_format not in ['mp3', 'm4a', 'wav', 'opus'] else [{
                    'key': 'FFmpegExtractAudio',
                    'preferredcodec': output_format,
                    'preferredquality': '192' if output_format == 'mp3' else '0',
                }],
            }
            
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
            
            download_status[download_id]['status'] = 'completed'
            download_status[download_id]['progress'] = 100
            download_status[download_id]['files'] = [str(output_path)]
        else:
            # Baixar vídeo completo primeiro e depois dividir em capítulos
            temp_video = TEMP_FOLDER / f"temp_{download_id}.mp4"
            
            ydl_opts = {
                'format': format_string,
                'outtmpl': str(temp_video),
                'progress_hooks': [lambda d: update_progress(d, download_id)],
                'merge_output_format': 'mp4',
                'noplaylist': True,
                'http_headers': get_default_headers(),
            }
            
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
            
            # Dividir em capítulos selecionados
            download_status[download_id]['status'] = 'splitting'
            output_files = []
            
            for chapter_id in selected_chapters:
                chapter = video_info['chapters'][chapter_id]
                chapter_title = sanitize_filename(chapter['title'])
                output_file = TEMP_FOLDER / f"{video_title} - {chapter_title}.{output_format}"
                
                start_time = format_time(chapter['start_time'])
                duration = chapter['end_time'] - chapter['start_time']
                
                # Usar FFmpeg para extrair o capítulo
                import subprocess
                
                # Se for áudio apenas, extrair áudio
                if output_format in ['mp3', 'm4a', 'wav', 'opus']:
                    cmd = [
                        'ffmpeg',
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
                        'ffmpeg',
                        '-i', str(temp_video),
                        '-ss', start_time,
                        '-t', str(duration),
                        '-c', 'copy',
                        '-y',
                        str(output_file)
                    ]
                
                subprocess.run(cmd, capture_output=True)
                output_files.append(str(output_file))
            
            # Remover arquivo temporário
            if temp_video.exists():
                temp_video.unlink()
            
            download_status[download_id]['status'] = 'completed'
            download_status[download_id]['progress'] = 100
            download_status[download_id]['files'] = output_files
            
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

@app.route('/favicon.ico')
def favicon():
    """Serve favicon"""
    return send_file(
        Path(__file__).parent / 'favicon.ico',
        mimetype='image/vnd.microsoft.icon'
    )

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
    download_type = data.get('download_type', 'chapters')  # 'chapters' ou 'complete'
    
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
    """Baixa um arquivo e deleta após envio"""
    file_path = TEMP_FOLDER / filename
    
    if not file_path.exists():
        return jsonify({'success': False, 'error': 'Arquivo não encontrado'}), 404
    
    try:
        # Enviar arquivo com streaming (sem salvar duplicado)
        response = send_file(
            file_path,
            as_attachment=True,
            download_name=filename
        )
        
        # Agendar deleção do arquivo após envio (após 2 segundos)
        def delete_file_delayed():
            time.sleep(2)
            try:
                if file_path.exists():
                    os.remove(file_path)
                    print(f"✓ Arquivo temporário deletado: {filename}")
            except Exception as e:
                print(f"⚠️ Erro ao deletar arquivo: {e}")
        
        thread = threading.Thread(target=delete_file_delayed, daemon=True)
        thread.start()
        
        return response
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

if __name__ == '__main__':
    print("🚀 Iniciando YouTube Chapter Downloader...")
    print("📱 Acesse: http://localhost:5000")
    app.run(debug=True, host='0.0.0.0', port=5000)
