# Guia de Build de Produção - Snap App

## 📱 Visão Geral

Este guia fornece instruções completas para gerar um APK de produção assinado do aplicativo Snap, pronto para distribuição na Google Play Store ou outras plataformas.

---

## 🔑 Passo 1: Criar o Keystore

Um keystore é necessário para assinar seu APK. **GUARDE ESTE ARQUIVO EM LOCAL SEGURO!**

### 1.1 Gerar Keystore

```bash
cd android/app
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias snap-release
```

### 1.2 Informações Solicitadas

Durante a criação, você será solicitado a fornecer:

- **Senha do keystore**: Escolha uma senha forte (mínimo 6 caracteres)
- **Senha da chave**: Pode ser a mesma do keystore
- **Nome e sobrenome**: Seu nome ou nome da empresa
- **Unidade organizacional**: Nome do departamento (opcional)
- **Organização**: Nome da empresa
- **Cidade/Localidade**: Sua cidade
- **Estado/Província**: Seu estado
- **Código do país**: BR (para Brasil)

### 1.3 Exemplo de Output

```
Gerando par de chaves RSA de 2.048 bits e certificado autoassinado (SHA256withRSA)
        com validade de 10.000 dias para: CN=João Silva, OU=Dev, O=Snap, L=São Paulo, ST=SP, C=BR
Digite a senha da chave para <snap-release>
        (RETURN se for igual à senha da área de armazenamento de chaves):
[Armazenando release-keystore.jks]
```

### 1.4 Importante - Backup

⚠️ **CRÍTICO**: Faça backup do arquivo `release-keystore.jks` em local seguro. Se você perder este arquivo ou as senhas, **não será possível atualizar seu app na Play Store!**

Sugestões de backup:
- Drive seguro na nuvem (Google Drive, Dropbox)
- Gerenciador de senhas (1Password, LastPass)
- Repositório privado (GitHub com acesso restrito)

---

## 🔐 Passo 2: Configurar Credenciais

### 2.1 Criar arquivo keystore.properties

Crie o arquivo `android/keystore.properties` (este arquivo está no .gitignore):

```properties
storePassword=SUA_SENHA_DO_KEYSTORE
keyPassword=SUA_SENHA_DA_CHAVE
keyAlias=snap-release
storeFile=release-keystore.jks
```

### 2.2 Atualizar build.gradle.kts

Descomente as linhas no arquivo `android/app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release-keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "sua_senha"
        keyAlias = System.getenv("KEY_ALIAS") ?: "snap-release"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "sua_senha"
    }
}

buildTypes {
    release {
        // ...
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### 2.3 Alternativa - Variáveis de Ambiente

Para maior segurança, use variáveis de ambiente:

**Windows:**
```cmd
set KEYSTORE_PASSWORD=sua_senha
set KEY_ALIAS=snap-release
set KEY_PASSWORD=sua_senha
```

**Linux/Mac:**
```bash
export KEYSTORE_PASSWORD=sua_senha
export KEY_ALIAS=snap-release
export KEY_PASSWORD=sua_senha
```

---

## 🏗️ Passo 3: Build do APK de Produção

### 3.1 Limpar builds anteriores

```bash
cd android
./gradlew clean
```

### 3.2 Gerar APK Release

```bash
./gradlew assembleRelease
```

### 3.3 Gerar AAB (Android App Bundle) - Recomendado para Play Store

```bash
./gradlew bundleRelease
```

### 3.4 Localizar os arquivos gerados

**APK:**
```
android/app/build/outputs/apk/release/app-release.apk
```

**AAB:**
```
android/app/build/outputs/bundle/release/app-release.aab
```

---

## ✅ Passo 4: Verificar o Build

### 4.1 Informações do APK

```bash
# Verificar assinatura
jarsigner -verify -verbose -certs android/app/build/outputs/apk/release/app-release.apk

# Ver informações detalhadas
aapt dump badging android/app/build/outputs/apk/release/app-release.apk
```

### 4.2 Tamanho do APK

O APK deve ter aproximadamente:
- **Debug**: ~15-20 MB
- **Release (otimizado)**: ~8-12 MB

### 4.3 ProGuard Mapping

Arquivo de mapeamento gerado em:
```
android/app/build/outputs/mapping/release/mapping.txt
```

⚠️ **IMPORTANTE**: Guarde este arquivo! Ele é necessário para:
- Desobfuscar stack traces de crashes
- Fazer upload na Play Console para análise de erros

---

## 🧪 Passo 5: Testar o APK

### 5.1 Instalar em Dispositivo

```bash
adb install android/app/build/outputs/apk/release/app-release.apk
```

### 5.2 Checklist de Testes

- [ ] App instala sem erros
- [ ] Todas as telas carregam corretamente
- [ ] Download de vídeos funciona
- [ ] Notificações aparecem
- [ ] Histórico salva downloads
- [ ] Estatísticas mostram dados corretos
- [ ] Compartilhamento funciona
- [ ] Widget funciona na home screen
- [ ] Temas (claro/escuro) funcionam
- [ ] Preferências de notificação funcionam
- [ ] Nenhum crash ou erro

---

## 📊 Passo 6: Análise do Build

### 6.1 Analisar tamanho do APK

```bash
./gradlew :app:analyzeReleaseBundle
```

### 6.2 Build Scan (opcional)

```bash
./gradlew assembleRelease --scan
```

Isso gera uma URL com análise detalhada do build.

---

## 🚀 Passo 7: Preparar para Distribuição

### 7.1 Google Play Store

1. **AAB é obrigatório** para novos apps
2. Faça upload do `app-release.aab`
3. Inclua o `mapping.txt` para análise de crashes
4. Configure versioning:
   - `versionCode`: Incrementar a cada release (1, 2, 3...)
   - `versionName`: Versão semântica (1.0.0, 1.0.1, 1.1.0...)

### 7.2 Distribuição Direta

1. Use o `app-release.apk` para distribuição fora da Play Store
2. Usuários precisam habilitar "Instalar apps de fontes desconhecidas"
3. Considere usar plataformas como:
   - Firebase App Distribution
   - Amazon Appstore
   - APKPure
   - Site próprio

---

## 🔄 Passo 8: Atualizações Futuras

### 8.1 Incrementar Versão

Antes de cada release, atualize em `build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Incrementar +1
    versionName = "1.0.1"  // Atualizar semanticamente
}
```

### 8.2 Changelog

Mantenha um arquivo `CHANGELOG.md` com as mudanças de cada versão:

```markdown
# Changelog

## [1.0.1] - 2024-01-15
### Fixed
- Corrigido crash ao baixar vídeos grandes
- Melhorada performance das notificações

### Added
- Suporte para novos formatos de vídeo
```

---

## 🛠️ Comandos Úteis

```bash
# Versão do Gradle
./gradlew --version

# Listar tasks disponíveis
./gradlew tasks

# Build debug + install
./gradlew installDebug

# Limpar + build release
./gradlew clean assembleRelease

# Rodar testes
./gradlew test

# Gerar relatório de dependências
./gradlew :app:dependencies
```

---

## ⚠️ Troubleshooting

### Erro: "jarsigner: unable to sign jar"

**Solução**: Verifique as credenciais no `keystore.properties` ou variáveis de ambiente.

### Erro: "Task :app:lintVitalRelease FAILED"

**Solução**: Desabilite temporariamente o lint ou corrija os warnings:
```kotlin
android {
    lint {
        abortOnError = false
    }
}
```

### APK muito grande

**Solução**: Verifique se ProGuard está habilitado:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
    }
}
```

### ProGuard quebra funcionalidade

**Solução**: Adicione regras ao `proguard-rules.pro` para as classes afetadas.

---

## 📞 Suporte

Para problemas ou dúvidas:
- Documentação oficial: https://developer.android.com/studio/publish
- Stack Overflow: https://stackoverflow.com/questions/tagged/android
- Android Developers Slack: https://developer.android.com/community

---

**Última atualização**: Janeiro 2024  
**Versão do guia**: 1.0
