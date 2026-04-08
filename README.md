# Snap - Video Downloader 🎬

Um downloader web moderno e inteligente para vídeos com suporte a capítulos, múltiplos formatos e interface elegante.

## 🆕 Atualizações Recentes

- Melhor tratamento de bloqueios do YouTube (anti-bot/autenticação).
- Fluxo de cookies reforçado:
  - tentativa automática de leitura do navegador;
  - fallback com importação manual de `cookies.txt` pela interface.
- Mensagens de erro mais claras para diferenciar:
  - vídeo indisponível/privado/restrito;
  - falha de autenticação anti-bot.
- Compatibilidade com `yt-dlp` atualizada para usar `js_runtimes` no formato correto.

## 🚀 Instalação Rápida (Funciona em Qualquer Máquina)

### 1. Instalar Python 3.8+
- **Windows**: [Download Python](https://www.python.org/downloads/)
- **macOS**: `brew install python3`
- **Linux**: `sudo apt install python3 python3-pip`

### 2. Clonar o projeto
```bash
git clone https://github.com/MikhaelGois/snap.git
cd snap
```

### 3. Instalar dependências
```bash
pip install -r requirements.txt
```

### 4. Rodar a aplicação
```bash
python app.py
```

**Pronto!** A aplicação vai:
- ✅ Abrir automaticamente em `http://localhost:5000`
- ✅ Detectar o FFmpeg instalado no sistema
- ✅ **Se não encontrar, baixar automaticamente o FFmpeg portátil**
- ✅ Funcionar sem configuração adicional

## 🌟 Funcionalidades

- 🎯 **Download Inteligente**: Vídeos completos ou capítulos individuais
- 🎨 **Interface Moderna**: Design elegante com tema escuro
- 📊 **Progresso em Tempo Real**: Acompanhe o download ao vivo
- 🎭 **Badges de Sites**: Identificação visual de YouTube, Vimeo, etc.
- 🎵 **Múltiplos Formatos**: MP4, WebM, MP3, M4A e mais
- 📱 **Totalmente Responsivo**: Funciona em desktop, tablet e mobile
- 🎬 **Player Integrado**: Reproduza áudio/vídeo direto no navegador
- 📜 **Histórico**: Veja todos os seus downloads anteriores
- 🔄 **Sem Configuração**: FFmpeg é baixado automaticamente se necessário

## 💡 FFmpeg Automático

A aplicação detecta automaticamente o FFmpeg em:
- ✅ PATH do sistema
- ✅ Instalações comuns (Chocolatey, Scoop, etc.)
- ✅ Se não encontrar, **baixa automaticamente** uma versão portátil

**Você não precisa instalar nada manualmente!**

### (Opcional) Instalar FFmpeg Manualmente

Se preferir instalar globalmente:

#### Windows
```powershell
choco install ffmpeg
```

#### macOS
```bash
brew install ffmpeg
```

#### Linux
```bash
sudo apt install ffmpeg
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
