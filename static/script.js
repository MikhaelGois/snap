// Estado da aplicação
let currentVideoInfo = null;
let currentUrl = null;
let downloadId = null;
let progressInterval = null;

// Elementos DOM
const urlInput = document.getElementById('youtube-url');
const fetchBtn = document.getElementById('fetch-btn');
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
const videoQualityGroup = document.getElementById('video-quality-group');
const audioQualityGroup = document.getElementById('audio-quality-group');

// Formatos de áudio e vídeo
const audioFormats = ['mp3', 'm4a', 'wav', 'opus'];
const videoFormats = ['mp4', 'mkv', 'webm', 'avi'];

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

// Funções
function updateQualityOptions() {
    const selectedFormat = formatSelect.value;
    const isAudio = audioFormats.includes(selectedFormat);
    
    // Mostrar/esconder grupos de qualidade
    videoQualityGroup.style.display = isAudio ? 'none' : 'block';
    audioQualityGroup.style.display = isAudio ? 'block' : 'none';
    
    // Resetar seleção para a primeira qualidade disponível
    qualitySelect.value = 'best';
    
    console.log(`Formato selecionado: ${selectedFormat.toUpperCase()} (${isAudio ? 'Áudio' : 'Vídeo'})`);
}

function showLoader(button) {
    const btnText = button.querySelector('.btn-text');
    const loader = button.querySelector('.loader');
    btnText.style.display = 'none';
    loader.style.display = 'inline-block';
    button.disabled = true;
}

function hideLoader(button) {
    const btnText = button.querySelector('.btn-text');
    const loader = button.querySelector('.loader');
    btnText.style.display = 'inline';
    loader.style.display = 'none';
    button.disabled = false;
}

function showError(message) {
    errorMessage.textContent = `❌ ${message}`;
    errorMessage.style.display = 'block';
    setTimeout(() => {
        errorMessage.style.display = 'none';
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
    
    videoSection.style.display = 'block';

    // Mostrar capítulos ou mensagem de "sem capítulos"
    if (info.has_chapters && info.chapters.length > 0) {
        chaptersContainer.style.display = 'block';
        noChapters.style.display = 'none';
        document.getElementById('download-type-group').style.display = 'block';
        document.getElementById('chapters-info').style.display = 'block';
        displayChapters(info.chapters);
        updateDownloadInfo();
    } else {
        chaptersContainer.style.display = 'none';
        noChapters.style.display = 'block';
        document.getElementById('download-type-group').style.display = 'none';
        document.getElementById('chapters-info').style.display = 'none';
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
    
    // Atualizar contador
    document.getElementById('selected-chapters-count').textContent = selectedCount;
    
    if (selectedCount === 0) {
        downloadInfo.textContent = 'Selecione pelo menos um capítulo';
        downloadBtn.disabled = true;
    } else if (selectedCount === totalCount) {
        downloadInfo.textContent = 'Será baixado o vídeo completo ou capítulos separados (escolha acima)';
        downloadBtn.disabled = false;
    } else {
        downloadInfo.textContent = `Serão baixados ${selectedCount} de ${totalCount} capítulos`;
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
    videoSection.style.display = 'none';
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
        progressSection.style.display = 'none';
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
            progressSection.style.display = 'none';
            videoSection.style.display = 'block';
        }
    }, 1000);
}

function showDownloadComplete(files) {
    progressSection.style.display = 'none';
    completeSection.style.display = 'block';
    
    downloadLinks.innerHTML = '';
    
    files.forEach(filePath => {
        const fileName = filePath.split('\\').pop().split('/').pop();
        const link = document.createElement('a');
        link.href = `/api/download-file/${fileName}`;
        link.className = 'download-link';
        link.textContent = `📥 ${fileName}`;
        link.download = fileName;
        downloadLinks.appendChild(link);
    });
}

function resetApp() {
    urlInput.value = '';
    currentVideoInfo = null;
    currentUrl = null;
    downloadId = null;
    
    videoSection.style.display = 'none';
    progressSection.style.display = 'none';
    completeSection.style.display = 'none';
    errorMessage.style.display = 'none';
    
    progressBar.style.width = '0%';
    progressText.textContent = '0%';
}
