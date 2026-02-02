# Checklist Google Play Store - Snap App

## 📋 Visão Geral

Este documento contém todos os requisitos e materiais necessários para publicar o Snap na Google Play Store.

---

## 🎯 Requisitos Obrigatórios

### 1. Conta Google Play Console

- [ ] Criar conta em [Google Play Console](https://play.google.com/console)
- [ ] Pagar taxa única de registro ($25 USD)
- [ ] Verificar identidade e informações de contato
- [ ] Configurar perfil de desenvolvedor público

### 2. App Bundle (AAB)

- [ ] Gerar AAB assinado: `./gradlew bundleRelease`
- [ ] Arquivo deve estar em: `app/build/outputs/bundle/release/app-release.aab`
- [ ] Tamanho recomendado: < 150 MB
- [ ] Upload do arquivo `mapping.txt` para análise de crashes

### 3. Política de Privacidade

- [ ] Criar página web com política de privacidade
- [ ] URL pública e acessível permanentemente
- [ ] Incluir informações sobre:
  - Dados coletados (URL de download, histórico)
  - Armazenamento local (Room Database)
  - Permissões usadas (Internet, Notifications, Storage)
  - Compartilhamento de dados (nenhum - app offline)

---

## 🖼️ Materiais Gráficos

### 1. Ícone do App

- [x] Já criado em: `app/src/main/res/mipmap-*/ic_launcher.png`
- [ ] Verificar qualidade em todas as resoluções:
  - mdpi: 48x48 px
  - hdpi: 72x72 px
  - xhdpi: 96x96 px
  - xxhdpi: 144x144 px
  - xxxhdpi: 192x192 px

### 2. Feature Graphic (Obrigatório)

- [ ] Dimensões: **1024 x 500 pixels**
- [ ] Formato: PNG ou JPG
- [ ] Sem transparência
- [ ] Conteúdo: Banner promocional do app
- [ ] Texto visível e legível
- [ ] Sugestão: Logo do Snap + texto "Baixe vídeos facilmente"

### 3. Screenshots (Mínimo 2, Máximo 8)

#### Phone Screenshots
- [ ] Dimensões: Entre 320-3840 pixels em cada dimensão
- [ ] Proporção: Mínimo 2:1 (altura:largura)
- [ ] Formato: PNG ou JPG (24-bit)
- [ ] Screenshots necessárias:
  1. Tela inicial (URL input)
  2. Download em progresso (com notificação)
  3. Histórico de downloads
  4. Estatísticas
  5. Temas (claro/escuro)

#### Tablet Screenshots (Opcional)
- [ ] Dimensões: Entre 320-3840 pixels
- [ ] Mínimo 2 screenshots se suportar tablets

### 4. Vídeo Promocional (Opcional)

- [ ] Upload no YouTube
- [ ] Duração: 30 segundos a 2 minutos
- [ ] Mostrar principais funcionalidades
- [ ] Qualidade HD (1080p mínimo)

---

## 📝 Informações do App

### 1. Título

- [ ] Nome: **Snap - Downloader de Vídeos**
- [ ] Máximo: 50 caracteres
- [ ] Único na Play Store

### 2. Descrição Curta

- [ ] Máximo: 80 caracteres
- [ ] Exemplo: "Baixe e gerencie vídeos facilmente. Rápido, simples e eficiente."

### 3. Descrição Completa

- [ ] Máximo: 4000 caracteres
- [ ] Incluir:
  - Principais recursos
  - Como usar
  - Funcionalidades únicas
  - Requisitos do sistema

**Exemplo de descrição:**

```
📥 SNAP - O DOWNLOADER DE VÍDEOS MAIS SIMPLES E EFICIENTE

Baixe seus vídeos favoritos de forma rápida e organizada com o Snap! 
Interface moderna, notificações inteligentes e gerenciamento completo 
de downloads.

✨ PRINCIPAIS RECURSOS:

🎯 DOWNLOAD RÁPIDO
• Cole a URL e baixe instantaneamente
• Suporte para múltiplos formatos
• Download em segundo plano

📊 GERENCIAMENTO COMPLETO
• Histórico detalhado de downloads
• Estatísticas de uso
• Filtros por data e status

🔔 NOTIFICAÇÕES INTELIGENTES
• Progresso em tempo real
• Notificação de conclusão
• Personalização completa

🎨 DESIGN MODERNO
• Material Design 3
• Tema claro e escuro
• Suporte a Dynamic Colors (Android 12+)

📤 COMPARTILHAMENTO FÁCIL
• Compartilhe arquivos baixados
• Exporte estatísticas
• Histórico compartilhável

🏠 WIDGET NA TELA INICIAL
• Acesso rápido a downloads
• Sem abrir o app

🔒 PRIVACIDADE
• Sem coleta de dados pessoais
• Armazenamento local
• Sem anúncios

💪 REQUISITOS:
• Android 10 ou superior
• Conexão com internet
• 50MB de espaço livre

📱 SUPORTE:
Encontrou algum problema? Entre em contato: seu_email@exemplo.com

⭐ Avalie o Snap e ajude outros usuários!
```

### 4. Categoria

- [ ] Selecionar: **Ferramentas** ou **Multimídia e vídeo**
- [ ] Tags relevantes: download, vídeo, gerenciador

### 5. Informações de Contato

- [ ] Email de suporte obrigatório
- [ ] Website (opcional, mas recomendado)
- [ ] Telefone (opcional)

---

## 🔐 Privacidade e Segurança

### 1. Questionário de Segurança de Dados

- [ ] Responder questionário sobre coleta de dados
- [ ] Informações para o Snap:
  - **Dados coletados**: Nenhum dado pessoal
  - **Dados armazenados localmente**: URLs, histórico, preferências
  - **Compartilhamento**: Nenhum
  - **Criptografia**: SQLCipher no banco de dados (opcional)
  - **Permissões**: Internet, Notificações, Armazenamento

### 2. Declaração de Privacidade

Exemplo de conteúdo para página de privacidade:

```markdown
# Política de Privacidade - Snap App

## Coleta de Dados
O Snap não coleta, armazena ou compartilha dados pessoais dos usuários.

## Armazenamento Local
- URLs de download (apenas localmente)
- Histórico de downloads (apenas no dispositivo)
- Preferências do app (apenas no dispositivo)

## Permissões Necessárias
- **Internet**: Para realizar downloads
- **Notificações**: Para notificar progresso de downloads
- **Armazenamento**: Para salvar vídeos baixados

## Compartilhamento de Dados
Nenhum dado é compartilhado com terceiros.

## Contato
Email: seu_email@exemplo.com
```

### 3. Público-Alvo

- [ ] Classificação etária: **PEGI 3** ou **Livre**
- [ ] Conteúdo apropriado para todas as idades

---

## 🌍 Localização

### 1. Idiomas Suportados

- [x] Português (Brasil) - Principal
- [ ] Inglês (opcional, mas recomendado para alcance global)
- [ ] Espanhol (opcional)

### 2. Países de Distribuição

- [ ] Selecionar países onde o app estará disponível
- [ ] Recomendado: Todos os países (alcance global)
- [ ] Considerar restrições legais de cada país

---

## 💰 Precificação

- [ ] Modelo: **Gratuito**
- [ ] Sem compras no app (IAP)
- [ ] Sem anúncios

---

## 🎯 Configurações de Lançamento

### 1. Tipo de Lançamento

- [ ] **Produção**: Disponível para todos
- [ ] **Teste fechado**: Apenas testadores específicos
- [ ] **Teste aberto**: Qualquer um pode testar
- [ ] **Teste interno**: Apenas equipe de desenvolvimento

### 2. Gestão de Versões

- [ ] Lançamento gradual (opcional):
  - 10% dos usuários
  - 25% dos usuários
  - 50% dos usuários
  - 100% dos usuários

### 3. Países de Lançamento

- [ ] Brasil (primeiro lançamento)
- [ ] Expandir para outros países após estabilização

---

## ✅ Checklist Final Pré-Publicação

### Técnico
- [ ] APK/AAB assinado corretamente
- [ ] ProGuard mapping.txt incluído
- [ ] Versão correta (versionCode e versionName)
- [ ] Testado em múltiplos dispositivos
- [ ] Sem crashes críticos
- [ ] Performance aceitável

### Conteúdo
- [ ] Todos os screenshots adicionados
- [ ] Feature graphic criado
- [ ] Descrições em todos os idiomas
- [ ] Política de privacidade publicada
- [ ] Email de contato válido

### Legal
- [ ] Aceitar Termos de Serviço do Google Play
- [ ] Declaração de conteúdo
- [ ] Classificação etária
- [ ] Questionário de segurança de dados

### Qualidade
- [ ] Todas as funcionalidades testadas
- [ ] UI/UX polida
- [ ] Sem bugs conhecidos críticos
- [ ] Performance otimizada

---

## 📅 Timeline de Publicação

### 1ª Publicação

1. **Dia 1**: Criar conta Play Console ($25)
2. **Dia 2-3**: Preparar materiais gráficos
3. **Dia 4**: Escrever descrições e política de privacidade
4. **Dia 5**: Upload do AAB e preencher formulários
5. **Dia 6-7**: Aguardar revisão do Google (1-7 dias)
6. **Dia 8+**: App publicado na Play Store!

### Atualizações Futuras

- **Teste interno**: 1-2 dias
- **Teste fechado/aberto**: 1 semana
- **Produção**: Após testes bem-sucedidos

---

## 📊 Pós-Publicação

### 1. Monitoramento

- [ ] Acompanhar Play Console diariamente
- [ ] Verificar crashes (Firebase Crashlytics recomendado)
- [ ] Ler avaliações e responder usuários
- [ ] Monitorar métricas de instalação

### 2. Métricas Importantes

- Instalações diárias/mensais
- Desinstalações
- Retenção (D1, D7, D30)
- Crashes e ANRs
- Avaliação média (meta: ≥ 4.0 estrelas)

### 3. Marketing

- [ ] Compartilhar link na Play Store
- [ ] Criar landing page
- [ ] Promover em redes sociais
- [ ] Pedir avaliações de usuários

---

## 🔗 Links Úteis

- **Play Console**: https://play.google.com/console
- **Guia de Lançamento**: https://developer.android.com/distribute/best-practices/launch
- **Diretrizes de Design**: https://developer.android.com/distribute/google-play/resources/icon-design-specifications
- **Políticas do Google Play**: https://play.google.com/about/developer-content-policy/

---

**Última atualização**: Janeiro 2024  
**Status**: ✅ Pronto para publicação após completar checklist
