// Estado da aplicação
let currentVideoInfo = null;
let currentUrl = null;
let downloadId = null;
let progressInterval = null;

// Elementos DOM
const urlInput = document.getElementById('youtube-url');
const fetchBtn = document.getElementById('fetch-btn');
const multiUrlsInput = document.getElementById('multi-urls');
const fetchMultiBtn = document.getElementById('fetch-multi-btn');
const singleInputMode = document.getElementById('single-input-mode');
const multiInputMode = document.getElementById('multi-input-mode');
const singleModeBtn = document.getElementById('single-mode-btn');
const multiModeBtn = document.getElementById('multi-mode-btn');
const videoSection = document.getElementById('video-section');
const videoThumbnail = document.getElementById('video-thumbnail');
const videoTitle = document.getElementById('video-title');
const videoDuration = document.getElementById('video-duration');
const chaptersContainer = document.getElementById('chapters-container');
const chaptersList = document.getElementById('chapters-list');
const noChapters = document.getElementById('no-chapters');
const selectAllBtn = document.getElementById('select-all-btn');
const deselectAllBtn = document.getElementById('deselect-all-btn');
const downloadBtn = document.getElementById('download-btn');
const downloadInfo = document.getElementById('download-info');
const progressSection = document.getElementById('progress-section');
const progressBar = document.getElementById('progress-bar');
const progressText = document.getElementById('progress-text');
const progressStatus = document.getElementById('progress-status');
const completeSection = document.getElementById('complete-section');
const downloadLinks = document.getElementById('download-links');
const newDownloadBtn = document.getElementById('new-download-btn');
const errorMessage = document.getElementById('error-message');
const formatSelect = document.getElementById('format-select');
const qualitySelect = document.getElementById('quality-select');
const historySection = document.getElementById('history-section');
const historyList = document.getElementById('history-list');
const videoQualityGroup = document.getElementById('video-quality-group');
const audioQualityGroup = document.getElementById('audio-quality-group');
const playerSection = document.getElementById('player-section');
const playerPlaceholder = document.getElementById('player-placeholder');
const videoPlayer = document.getElementById('video-player');
const audioPlayer = document.getElementById('audio-player');
const playlistBadge = document.getElementById('playlist-badge');
const playlistCount = document.getElementById('playlist-count');
const siteBadge = document.getElementById('site-badge');
const historyBtn = document.getElementById('history-btn');
const historyCountBadge = document.querySelector('.badge-count');

// Formatos de áudio e vídeo
const audioFormats = ['mp3', 'm4a', 'wav', 'opus'];
const videoFormats = ['mp4', 'mkv', 'webm', 'avi'];

// Mapeamento de cores de sites
const siteBadgeColors = {
    'youtube': '#FF0000',
    'youtubemusic': '#FF0000',
    'vimeo': '#1AB7EA',
    'dailymotion': '#00A8E1',
    'soundcloud': '#FF7700',
    'spotify': '#1DB954',
    'twitch': '#9146FF',
    'instagram': '#E4405F',
    'tiktok': '#000000'
};

// Event Listeners
fetchBtn.addEventListener('click', fetchVideoInfo);
urlInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') fetchVideoInfo();
});
selectAllBtn.addEventListener('click', () => toggleAllChapters(true));
deselectAllBtn.addEventListener('click', () => toggleAllChapters(false));
downloadBtn.addEventListener('click', startDownload);
newDownloadBtn.addEventListener('click', resetApp);
formatSelect.addEventListener('change', updateQualityOptions);
if (historyBtn) historyBtn.addEventListener('click', toggleHistory);

// Event listeners para modo single/multi
singleModeBtn.addEventListener('click', () => switchMode('single'));
multiModeBtn.addEventListener('click', () => switchMode('multi'));
fetchMultiBtn.addEventListener('click', fetchMultipleUrls);

// Event listener para mudar tipo de download
document.addEventListener('change', (e) => {
    if (e.target.name === 'download-type') {
        updateDownloadInfo();
    }
});

// Carregar histórico ao iniciar
loadHistoryCount();

// Funções
function updateQualityOptions() {
    const selectedFormat = formatSelect.value;
    const isAudio = audioFormats.includes(selectedFormat);
    
    // Mostrar/esconder grupos de qualidade (se existirem no HTML)
    if (videoQualityGroup && audioQualityGroup) {
        videoQualityGroup.style.display = isAudio ? 'none' : 'block';
        audioQualityGroup.style.display = isAudio ? 'block' : 'none';
    }
    
    // Resetar seleção para a primeira qualidade disponível
    if (qualitySelect) {
        qualitySelect.value = 'best';
    }
    
    console.log(`Formato selecionado: ${selectedFormat.toUpperCase()} (${isAudio ? 'Áudio' : 'Vídeo'})`);
}

function showLoader(button) {
    const btnText = button.querySelector('.btn-content') || button.querySelector('.btn-text');
    const loader = button.querySelector('.loader');
    if (btnText) btnText.style.display = 'none';
    if (loader) {
        loader.classList.remove('hidden');
        loader.style.display = 'inline-block';
    }
    button.disabled = true;
}

function hideLoader(button) {
    const btnText = button.querySelector('.btn-content') || button.querySelector('.btn-text');
    const loader = button.querySelector('.loader');
    if (btnText) btnText.style.display = 'inline-flex';
    if (loader) {
        loader.style.display = 'none';
        loader.classList.add('hidden');
    }
    button.disabled = false;
}

function showError(message) {
    errorMessage.textContent = `❌ ${message}`;
    errorMessage.classList.remove('hidden');
    errorMessage.style.display = 'block';
    setTimeout(() => {
        errorMessage.style.display = 'none';
        errorMessage.classList.add('hidden');
    }, 5000);
}

function formatDuration(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    
    if (hours > 0) {
        return `${hours}h ${minutes}m ${secs}s`;
    } else if (minutes > 0) {
        return `${minutes}m ${secs}s`;
    } else {
        return `${secs}s`;
    }
}

function formatTime(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    
    const pad = (num) => num.toString().padStart(2, '0');
    
    if (hours > 0) {
        return `${pad(hours)}:${pad(minutes)}:${pad(secs)}`;
    } else {
        return `${pad(minutes)}:${pad(secs)}`;
    }
}

async function fetchVideoInfo() {
    const url = urlInput.value.trim();
    
    if (!url) {
        showError('Por favor, insira uma URL do YouTube');
        return;
    }

    console.log('Enviando requisição para:', url);
    showLoader(fetchBtn);
    errorMessage.style.display = 'none';
    errorMessage.classList.add('hidden');

    try {
        console.log('Fazendo fetch para /api/video-info...');
        const response = await fetch('/api/video-info', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ url })
        });

        console.log('Resposta recebida, status:', response.status);
        const data = await response.json();
        console.log('Dados recebidos:', data);

        if (!data.success) {
            throw new Error(data.error || 'Erro ao buscar informações do vídeo');
        }

        currentVideoInfo = data;
        currentUrl = url;
        displayVideoInfo(data);

    } catch (error) {
        console.error('Erro capturado:', error);
        showError(error.message);
    } finally {
        hideLoader(fetchBtn);
    }
}

function displayVideoInfo(info) {
    // Mostrar informações do vídeo
    videoThumbnail.src = info.thumbnail;
    videoTitle.textContent = info.title;
    videoDuration.textContent = formatDuration(info.duration);
    
    // Exibir Badge do Site
    const siteExtractor = (info.extractor || 'youtube').toLowerCase();
    siteBadge.textContent = siteExtractor.toUpperCase().replace(/_/g, ' ');
    const siteColor = siteBadgeColors[siteExtractor] || '#FF6B35';
    siteBadge.style.background = siteColor;
    siteBadge.style.display = 'flex';
    
    // Exibir Badge de Playlist
    if (info.is_playlist) {
        playlistBadge.classList.remove('hidden');
        playlistCount.textContent = info.playlist_count || 'múltiplos';
    } else {
        playlistBadge.classList.add('hidden');
    }

    videoSection.classList.remove('hidden');
    videoSection.style.display = 'block';

    // Mostrar capítulos ou mensagem de "sem capítulos"
    if (info.has_chapters && info.chapters.length > 0) {
        chaptersContainer.classList.remove('hidden');
        chaptersContainer.style.display = 'block';
        noChapters.classList.add('hidden');
        noChapters.style.display = 'none';
        displayChapters(info.chapters);
        updateDownloadInfo();
    } else {
        chaptersContainer.classList.add('hidden');
        chaptersContainer.style.display = 'none';
        noChapters.classList.remove('hidden');
        noChapters.style.display = 'block';
        downloadInfo.textContent = 'Será baixado o vídeo completo';
    }
}

function displayChapters(chapters) {
    chaptersList.innerHTML = '';
    
    chapters.forEach((chapter, index) => {
        const duration = chapter.end_time - chapter.start_time;
        const chapterDiv = document.createElement('div');
        chapterDiv.className = 'chapter-item';
        
        chapterDiv.innerHTML = `
            <input type="checkbox" class="chapter-checkbox" data-chapter-id="${chapter.id}" checked>
            <div class="chapter-info">
                <div class="chapter-title">${chapter.title}</div>
                <div class="chapter-time">
                    ${formatTime(chapter.start_time)} - ${formatTime(chapter.end_time)} 
                    (${formatDuration(duration)})
                </div>
            </div>
        `;
        
        chapterDiv.addEventListener('click', (e) => {
            if (e.target.tagName !== 'INPUT') {
                const checkbox = chapterDiv.querySelector('.chapter-checkbox');
                checkbox.checked = !checkbox.checked;
                chapterDiv.classList.toggle('selected', checkbox.checked);
                updateDownloadInfo();
            }
        });
        
        const checkbox = chapterDiv.querySelector('.chapter-checkbox');
        checkbox.addEventListener('change', () => {
            chapterDiv.classList.toggle('selected', checkbox.checked);
            updateDownloadInfo();
        });
        
        chapterDiv.classList.add('selected');
        chaptersList.appendChild(chapterDiv);
    });
}

function toggleAllChapters(select) {
    const checkboxes = document.querySelectorAll('.chapter-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.checked = select;
        checkbox.closest('.chapter-item').classList.toggle('selected', select);
    });
    updateDownloadInfo();
}

function updateDownloadInfo() {
    const checkboxes = document.querySelectorAll('.chapter-checkbox');
    const selectedCount = Array.from(checkboxes).filter(cb => cb.checked).length;
    const totalCount = checkboxes.length;
    const downloadType = document.querySelector('input[name="download-type"]:checked')?.value || 'single';
    
    if (selectedCount === 0) {
        downloadInfo.textContent = 'Selecione pelo menos um capítulo';
        downloadBtn.disabled = true;
    } else if (selectedCount === totalCount) {
        if (downloadType === 'single') {
            downloadInfo.textContent = '📦 Será baixado o vídeo completo como arquivo único';
        } else {
            downloadInfo.textContent = `📂 Serão baixados ${totalCount} capítulos separados`;
        }
        downloadBtn.disabled = false;
    } else {
        if (downloadType === 'single') {
            downloadInfo.textContent = `📦 Serão baixados ${selectedCount} capítulos mesclados em arquivo único`;
        } else {
            downloadInfo.textContent = `📂 Serão baixados ${selectedCount} de ${totalCount} capítulos separados`;
        }
        downloadBtn.disabled = false;
    }
}

function getSelectedChapters() {
    const checkboxes = document.querySelectorAll('.chapter-checkbox:checked');
    return Array.from(checkboxes).map(cb => parseInt(cb.dataset.chapterId));
}

async function startDownload() {
    const selectedChapters = getSelectedChapters();
    
    if (selectedChapters.length === 0 && currentVideoInfo.has_chapters) {
        showError('Selecione pelo menos um capítulo');
        return;
    }

    // Obter formato e qualidade selecionados
    const format = document.getElementById('format-select').value;
    const quality = document.getElementById('quality-select').value;
    const downloadType = document.querySelector('input[name="download-type"]:checked')?.value || 'chapters';

    showLoader(downloadBtn);
    videoSection.classList.add('hidden');
    videoSection.style.display = 'none';
    progressSection.classList.remove('hidden');
    progressSection.style.display = 'block';
    progressStatus.textContent = 'Iniciando download...';

    try {
        const response = await fetch('/api/download', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                url: currentUrl,
                video_info: currentVideoInfo,
                selected_chapters: selectedChapters,
                format: format,
                quality: quality,
                download_type: downloadType
            })
        });

        const data = await response.json();

        if (!data.success) {
            throw new Error(data.error || 'Erro ao iniciar download');
        }

        downloadId = data.download_id;
        monitorDownloadProgress();

    } catch (error) {
        showError(error.message);
        progressSection.classList.add('hidden');
        progressSection.style.display = 'none';
        videoSection.classList.remove('hidden');
        videoSection.style.display = 'block';
    } finally {
        hideLoader(downloadBtn);
    }
}

async function monitorDownloadProgress() {
    progressInterval = setInterval(async () => {
        try {
            const response = await fetch(`/api/download-status/${downloadId}`);
            const data = await response.json();

            if (!data.success) {
                throw new Error(data.error || 'Erro ao verificar status');
            }

            const status = data.status;
            const progress = data.progress;

            progressBar.style.width = `${progress}%`;
            progressText.textContent = `${progress}%`;

            if (status === 'downloading') {
                progressStatus.textContent = 'Baixando vídeo...';
            } else if (status === 'splitting') {
                progressStatus.textContent = 'Dividindo em capítulos...';
            } else if (status === 'completed') {
                clearInterval(progressInterval);
                showDownloadComplete(data.files);
            } else if (status === 'error') {
                clearInterval(progressInterval);
                throw new Error(data.error || 'Erro durante o download');
            }

        } catch (error) {
            clearInterval(progressInterval);
            showError(error.message);
            progressSection.classList.add('hidden');
            progressSection.style.display = 'none';
            videoSection.classList.remove('hidden');
            videoSection.style.display = 'block';
        }
    }, 1000);
}

function showDownloadComplete(files) {
    progressSection.classList.add('hidden');
    progressSection.style.display = 'none';
    completeSection.classList.remove('hidden');
    completeSection.style.display = 'block';
    
    downloadLinks.innerHTML = '';
    
    files.forEach(filePath => {
        const fileName = filePath.split('\\').pop().split('/').pop();
        const wrapper = document.createElement('div');
        wrapper.className = 'download-link-wrapper';
        wrapper.style.display = 'flex';
        wrapper.style.gap = '1rem';
        wrapper.style.alignItems = 'center';
        
        const link = document.createElement('a');
        link.href = `/api/download-file/${encodeURIComponent(fileName)}`;
        link.className = 'download-link';
        link.style.flex = '1';
        link.innerHTML = `
            <span class="download-link-name">📥 ${fileName}</span>
            <span class="download-link-icon">⬇️</span>
        `;
        link.download = fileName;
        link.target = '_blank';
        
        const playBtn = document.createElement('button');
        playBtn.className = 'btn btn-secondary btn-sm';
        playBtn.innerHTML = '▶️ Reproduzir';
        playBtn.onclick = (e) => {
            e.preventDefault();
            playDownload(filePath);
        };
        
        wrapper.appendChild(link);
        wrapper.appendChild(playBtn);
        downloadLinks.appendChild(wrapper);
    });
    
    // Não reproduzir automaticamente - usuário clica em "Reproduzir" se quiser
    
    // Recarregar contagem do histórico
    loadHistoryCount();
}

function resetApp() {
    urlInput.value = '';
    multiUrlsInput.value = '';
    currentVideoInfo = null;
    currentUrl = null;
    downloadId = null;
    
    videoSection.classList.add('hidden');
    progressSection.classList.add('hidden');
    completeSection.classList.add('hidden');
    playerSection.classList.add('hidden');
    videoSection.style.display = 'none';
    progressSection.style.display = 'none';
    completeSection.style.display = 'none';
    playerSection.style.display = 'none';
    errorMessage.style.display = 'none';
    errorMessage.classList.add('hidden');
    
    progressBar.style.width = '0%';
    progressText.textContent = '0%';
}

// Funções de modo Single/Multi
function switchMode(mode) {
    if (mode === 'single') {
        singleInputMode.classList.remove('hidden');
        multiInputMode.classList.add('hidden');
        singleModeBtn.classList.add('active');
        multiModeBtn.classList.remove('active');
    } else {
        singleInputMode.classList.add('hidden');
        multiInputMode.classList.remove('hidden');
        singleModeBtn.classList.remove('active');
        multiModeBtn.classList.add('active');
    }
}

async function fetchMultipleUrls() {
    const urls = multiUrlsInput.value
        .split('\n')
        .map(url => url.trim())
        .filter(url => url.length > 0);
    
    if (urls.length === 0) {
        showError('Cole pelo menos uma URL');
        return;
    }
    
    showLoader(fetchMultiBtn);
    
    // Esconder seções anteriores
    videoSection.classList.add('hidden');
    videoSection.style.display = 'none';
    
    // Mostrar progresso
    progressSection.classList.remove('hidden');
    progressSection.style.display = 'block';
    progressStatus.textContent = `Baixando ${urls.length} vídeos...`;
    
    let completed = 0;
    const totalUrls = urls.length;
    
    for (const url of urls) {
        try {
            // Buscar info do vídeo
            const infoResponse = await fetch('/api/video-info', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ url })
            });
            
            const info = await infoResponse.json();
            
            if (!info.success) {
                console.error(`Erro ao buscar ${url}:`, info.error);
                completed++;
                continue;
            }
            
            // Iniciar download (vídeo completo, melhor qualidade, MP4)
            const downloadResponse = await fetch('/api/download', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    url: url,
                    video_info: info,
                    selected_chapters: [],
                    format: 'mp4',
                    quality: 'best',
                    download_type: 'single'
                })
            });
            
            const downloadData = await downloadResponse.json();
            
            if (downloadData.success) {
                // Monitorar progresso
                await monitorDownload(downloadData.download_id);
            }
            
            completed++;
            const progress = Math.round((completed / totalUrls) * 100);
            progressBar.style.width = `${progress}%`;
            progressText.textContent = `${progress}%`;
            progressStatus.textContent = `${completed}/${totalUrls} vídeos baixados`;
            
        } catch (error) {
            console.error(`Erro ao processar ${url}:`, error);
            completed++;
        }
    }
    
    hideLoader(fetchMultiBtn);
    
    // Mostrar seção de completo
    progressSection.classList.add('hidden');
    progressSection.style.display = 'none';
    completeSection.classList.remove('hidden');
    completeSection.style.display = 'block';
    
    // Carregar todos os arquivos baixados
    await loadAllDownloadedFiles();
}

async function monitorDownload(downloadId) {
    return new Promise((resolve) => {
        const interval = setInterval(async () => {
            try {
                const response = await fetch(`/api/download-status/${downloadId}`);
                const data = await response.json();
                
                if (data.status === 'completed' || data.status === 'error') {
                    clearInterval(interval);
                    resolve();
                }
            } catch (error) {
                clearInterval(interval);
                resolve();
            }
        }, 1000);
    });
}

async function loadAllDownloadedFiles() {
    try {
        const response = await fetch('/api/history');
        const data = await response.json();
        
        if (data.success && data.history.length > 0) {
            downloadLinks.innerHTML = '';
            
            // Pegar os últimos 10 downloads
            const recentDownloads = data.history.slice(-10).reverse();
            
            recentDownloads.forEach(item => {
                if (item.files && item.files.length > 0) {
                    item.files.forEach(filePath => {
                        const fileName = filePath.split('\\\\').pop().split('/').pop();
                        
                        const wrapper = document.createElement('div');
                        wrapper.className = 'download-link-wrapper';
                        
                        const link = document.createElement('a');
                        link.href = `/api/download-file/${encodeURIComponent(fileName)}`;
                        link.className = 'download-link';
                        link.innerHTML = `
                            <span class="download-link-name">📥 ${fileName}</span>
                            <span class="download-link-icon">⬇️</span>
                        `;
                        link.download = fileName;
                        link.target = '_blank';
                        
                        const playBtn = document.createElement('button');
                        playBtn.className = 'btn btn-secondary btn-sm';
                        playBtn.innerHTML = '▶️ Reproduzir';
                        playBtn.onclick = (e) => {
                            e.preventDefault();
                            playDownload(filePath);
                        };
                        
                        wrapper.appendChild(link);
                        wrapper.appendChild(playBtn);
                        downloadLinks.appendChild(wrapper);
                    });
                }
            });
        }
    } catch (error) {
        console.error('Erro ao carregar arquivos:', error);
    }
}

// Funções do Histórico
async function toggleHistory() {
    if (historySection.style.display === 'none') {
        await loadHistory();
        historySection.style.display = 'block';
        // Scroll suave até o histórico
        historySection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } else {
        historySection.style.display = 'none';
    }
}

async function loadHistory() {
    try {
        const response = await fetch('/api/history');
        const data = await response.json();
        
        if (!data.success) {
            throw new Error('Erro ao carregar histórico');
        }
        
        if (data.history.length === 0) {
            historyList.innerHTML = '<p class="no-history">📭 Nenhum download no histórico</p>';
            return;
        }
        
        // Renderizar histórico (mais recente primeiro)
        historyList.innerHTML = data.history.reverse().map(item => {
            const filesHtml = item.files.map(file => {
                const filename = file.split('\\').pop().split('/').pop();
                return `<a href="/api/download-file/${encodeURIComponent(filename)}" class="file-link" download="${filename}">📥 ${filename}</a>`;
            }).join('');
            
            return `
                <div class="history-item">
                    <div class="history-header">
                        <h4>🎬 ${item.title}</h4>
                        <span class="history-date">📅 ${item.timestamp}</span>
                    </div>
                    <p class="history-info">
                        <strong>Formato:</strong> ${item.format.toUpperCase()} | 
                        <strong>Qualidade:</strong> ${item.quality} | 
                        <strong>Arquivos:</strong> ${item.files.length}
                    </p>
                    <div class="history-files">
                        ${filesHtml}
                    </div>
                    <p class="history-url"><strong>URL:</strong> <a href="${item.url}" target="_blank">${item.url}</a></p>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Erro ao carregar histórico:', error);
        showError('Erro ao carregar histórico: ' + error.message);
    }
}

async function clearHistory() {
    if (!confirm('⚠️ Tem certeza que deseja limpar todo o histórico de downloads?\n\nEsta ação não pode ser desfeita.')) {
        return;
    }
    
    try {
        const response = await fetch('/api/history/clear', {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (!data.success) {
            throw new Error('Erro ao limpar histórico');
        }
        
        // Recarregar histórico vazio
        await loadHistory();
        
        // Feedback visual
        showError('✅ Histórico limpo com sucesso!');
        setTimeout(() => {
            errorMessage.style.display = 'none';
            errorMessage.classList.add('hidden');
        }, 3000);
        
    } catch (error) {
        console.error('Erro ao limpar histórico:', error);
        showError('❌ Erro ao limpar histórico: ' + error.message);
    }
}

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    console.log('📹 Video Splitter Downloader carregado!');
    console.log('🌍 Suporta YouTube, Vimeo, Dailymotion e mais de 1000 sites!');
    loadHistoryCount();
});

// Funções de Player
function playDownload(filePath) {
    const filename = filePath.split('\\').pop().split('/').pop();
    const ext = filename.split('.').pop().toLowerCase();
    
    // Detectar tipo de arquivo
    const audioExtensions = ['mp3', 'm4a', 'wav', 'opus', 'aac', 'flac'];
    const videoExtensions = ['mp4', 'mkv', 'webm', 'avi', 'mov', 'flv'];
    
    const isAudio = audioExtensions.includes(ext);
    const isVideo = videoExtensions.includes(ext);
    
    if (isAudio) {
        audioPlayer.src = `/api/download-file/${encodeURIComponent(filename)}`;
        audioPlayer.style.display = 'block';
        videoPlayer.style.display = 'none';
        playerPlaceholder.style.display = 'none';
        playerSection.classList.remove('hidden');
        playerSection.style.display = 'block';
        // Não fazer auto-play - deixar usuário controlar
    } else if (isVideo) {
        videoPlayer.src = `/api/download-file/${encodeURIComponent(filename)}`;
        videoPlayer.style.display = 'block';
        audioPlayer.style.display = 'none';
        playerPlaceholder.style.display = 'none';
        playerSection.classList.remove('hidden');
        playerSection.style.display = 'block';
        // Não fazer auto-play - deixar usuário controlar
    }
    
    // Scroll suave até o player
    playerSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Carregar contagem de histórico
async function loadHistoryCount() {
    try {
        const response = await fetch('/api/history');
        const data = await response.json();
        
        if (data.success && data.history.length > 0 && historyCountBadge) {
            historyCountBadge.textContent = data.history.length;
            historyCountBadge.style.display = 'flex';
        } else if (historyCountBadge) {
            historyCountBadge.style.display = 'none';
        }
    } catch (error) {
        console.error('Erro ao carregar contagem do histórico:', error);
    }
}
