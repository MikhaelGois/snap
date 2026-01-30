# YouTube Chapter Downloader 📹

Um downloader web moderno para vídeos do YouTube com suporte a seleção de capítulos individuais.

## 🌟 Funcionalidades

- ✅ Download de vídeos completos do YouTube
- ✅ Detecção automática de capítulos
- ✅ Seleção individual de capítulos para download
- ✅ Interface web moderna e responsiva
- ✅ Barra de progresso em tempo real
- ✅ Download de múltiplos capítulos simultaneamente

## 📋 Pré-requisitos

Antes de começar, você precisa ter instalado:

1. **Python 3.8+** - [Download Python](https://www.python.org/downloads/)
2. **FFmpeg** - Necessário para dividir vídeos em capítulos

### Instalando FFmpeg

#### Windows
1. Baixe o FFmpeg do site oficial: https://ffmpeg.org/download.html
2. Extraia o arquivo ZIP
3. Adicione a pasta `bin` ao PATH do sistema:
   - Pesquise "Variáveis de ambiente" no menu Iniciar
   - Clique em "Variáveis de Ambiente"
   - Em "Variáveis do sistema", encontre "Path" e clique em "Editar"
   - Clique em "Novo" e adicione o caminho para a pasta `bin` do FFmpeg
   - Exemplo: `C:\ffmpeg\bin`

**OU use Chocolatey:**
```powershell
choco install ffmpeg
```

#### macOS
```bash
brew install ffmpeg
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install ffmpeg
```

### Verificando a instalação do FFmpeg
```bash
ffmpeg -version
```

## 🚀 Instalação

1. **Clone ou baixe este repositório**

2. **Instale as dependências Python:**
```bash
pip install -r requirements.txt
```

## ▶️ Como Usar

1. **Inicie o servidor:**
```bash
python app.py
```

2. **Abra seu navegador e acesse:**
```
http://localhost:5000
```

3. **Cole a URL de um vídeo do YouTube**

4. **Selecione os capítulos desejados** (se o vídeo tiver capítulos)

5. **Clique em "Baixar"** e aguarde o download

6. **Os arquivos estarão disponíveis** na pasta `downloads/`

## 📁 Estrutura do Projeto

```
snap/
├── app.py                  # Backend Flask
├── requirements.txt        # Dependências Python
├── templates/
│   └── index.html         # Interface HTML
├── static/
│   ├── style.css          # Estilos CSS
│   └── script.js          # Lógica JavaScript
└── downloads/             # Pasta de downloads (criada automaticamente)
```

## 🎯 Como Funciona

1. **Frontend**: Interface web moderna que se comunica com o backend via API REST
2. **Backend**: Servidor Flask que processa requisições e gerencia downloads
3. **yt-dlp**: Biblioteca Python que extrai informações e baixa vídeos do YouTube
4. **FFmpeg**: Ferramenta que divide vídeos em capítulos baseado nos timestamps

## 🔧 Funcionalidades Técnicas

### API Endpoints

- `GET /` - Página principal
- `POST /api/video-info` - Obtém informações do vídeo e capítulos
- `POST /api/download` - Inicia o download
- `GET /api/download-status/<id>` - Verifica status do download
- `GET /api/download-file/<filename>` - Baixa o arquivo

### Fluxo de Download

1. Usuário insere URL do vídeo
2. Backend extrai informações usando yt-dlp
3. Se o vídeo tem capítulos, exibe lista para seleção
4. Usuário seleciona capítulos desejados
5. Backend baixa vídeo completo
6. FFmpeg divide o vídeo nos capítulos selecionados
7. Arquivos ficam disponíveis para download

## ⚠️ Limitações

- Nem todos os vídeos do YouTube possuem capítulos marcados
- Vídeos muito longos podem demorar para processar
- É necessário ter espaço em disco suficiente
- Respeite os direitos autorais dos criadores de conteúdo

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:
- Reportar bugs
- Sugerir novas funcionalidades
- Enviar pull requests

## 📝 Licença

Este projeto é apenas para fins educacionais. Use com responsabilidade e respeite os termos de serviço do YouTube.

## 💡 Dicas

- Use URLs completas do YouTube (não URLs encurtadas)
- Para vídeos sem capítulos, o vídeo completo será baixado
- A primeira execução pode ser mais lenta devido ao download do yt-dlp
- Mantenha o yt-dlp atualizado: `pip install --upgrade yt-dlp`

## 🐛 Solução de Problemas

### "FFmpeg não encontrado"
- Certifique-se de que o FFmpeg está instalado e no PATH do sistema
- Reinicie o terminal após adicionar o FFmpeg ao PATH

### "Erro ao baixar vídeo"
- Verifique se a URL está correta
- Alguns vídeos podem ter restrições de região
- Atualize o yt-dlp: `pip install --upgrade yt-dlp`

### Downloads não aparecem
- Verifique a pasta `downloads/` no diretório do projeto
- Verifique permissões de escrita na pasta

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique se todas as dependências estão instaladas
2. Confirme que o FFmpeg está no PATH
3. Verifique os logs do console para mensagens de erro

---

**Desenvolvido com ❤️ usando Flask, yt-dlp e FFmpeg**
