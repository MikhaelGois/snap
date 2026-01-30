# 🚀 Guia de Deployment

Este projeto é uma **aplicação Flask com backend Python**, portanto não pode ser hospedado diretamente no GitHub Pages. Abaixo estão as melhores opções para publicar sua aplicação gratuitamente:

## 📌 Opções Recomendadas

### 1️⃣ **Replit** (Mais Fácil) ⭐
A forma mais simples e rápida para começar:

1. Acesse https://replit.com
2. Clique em **"Create"** → **"Import from GitHub"**
3. Cole: `https://github.com/MikhaelGois/snap`
4. Clique em **"Import"**
5. Replit automaticamente detectará o `requirements.txt` e instalará as dependências
6. Clique em **"Run"** para iniciar
7. Sua app estará disponível em um URL como: `https://snap-seu-username.replit.dev`

**Vantagens:**
- ✅ Configuração automática
- ✅ Não precisa de terminal
- ✅ Hospedagem gratuita com limites generosos
- ✅ Compartilhar é fácil

---

### 2️⃣ **Heroku** (Tradicional)
Plataforma popular para apps Python:

**Passos:**

1. Crie conta em https://heroku.com
2. Instale o Heroku CLI: https://devcenter.heroku.com/articles/heroku-cli
3. No PowerShell, na pasta do projeto:

```powershell
heroku login
heroku create seu-nome-de-app
git push heroku main
heroku open
```

**Observação:** Heroku mudou seu modelo de preços em novembro de 2022. Você pode usar alternativas gratuitas como Railway ou Render.

---

### 3️⃣ **Railway** (Recomendado 2024) ⭐
A melhor alternativa ao Heroku:

1. Acesse https://railway.app
2. Clique em **"Deploy Now"**
3. Selecione **"Deploy from GitHub repo"**
4. Escolha seu repositório `snap`
5. Railway automaticamente configurará tudo
6. Sua app estará em produção em minutos

**Vantagens:**
- ✅ $5 de crédito gratuito por mês
- ✅ Fácil integração com GitHub
- ✅ Deploy automático ao fazer push
- ✅ Suporta Python, Node.js, etc.

---

### 4️⃣ **Render** (Alternativa)
Similar ao Railway:

1. Acesse https://render.com
2. Clique em **"New +"** → **"Web Service"**
3. Conecte seu GitHub
4. Selecione o repositório `snap`
5. Preencha as configurações:
   - **Build Command:** `pip install -r requirements.txt`
   - **Start Command:** `gunicorn app:app`
6. Deploy!

---

## 🔧 Configuração de Variáveis de Ambiente

Se precisar de variáveis de ambiente (API keys, etc), adicione em:
- **Replit:** Secrets (ícone 🔐)
- **Railway:** Variables
- **Render:** Environment

---

## 📊 Comparação de Plataformas

| Plataforma | Custo | Facilidade | Vantagens |
|-----------|--------|----------|-----------|
| **Replit** | Gratuito | ⭐⭐⭐⭐⭐ | Mais fácil de usar |
| **Railway** | $5/mês | ⭐⭐⭐⭐ | Melhor que Heroku |
| **Render** | Gratuito | ⭐⭐⭐⭐ | Bom suporte |
| **Heroku** | Pago | ⭐⭐⭐⭐ | Mais histórico |

---

## 🎯 Recomendação Final

**Para começar agora:** Use **Replit** (5 minutos de setup)
**Para produção:** Use **Railway** (melhor que Heroku, mais confiável)

---

## 📝 Troubleshooting

**Erro: "ffmpeg not found"**
- As plataformas de cloud já têm FFmpeg instalado
- Se não, configure na plataforma para instalar dependências do sistema

**Erro: "Port 5000 already in use"**
- A app automaticamente usa a PORT fornecida pela plataforma
- O código já está configurado para isso

**Downloads não funcionam**
- Em produção, use storage em nuvem (AWS S3, Google Cloud Storage)
- Para teste, downloads funcionam normalmente

---

## 📚 Recursos Adicionais

- [Documentação Railway](https://docs.railway.app)
- [Documentação Render](https://render.com/docs)
- [Documentação Replit](https://docs.replit.com)
- [Guia Flask Deployment](https://flask.palletsprojects.com/deployment/)
