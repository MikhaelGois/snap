# 📥 Snap - Video Downloader

<div align="center">
  <p><strong>Downloader de Vídeos Simples e Eficiente para Android</strong></p>
  
  <p>
    <img src="https://img.shields.io/badge/Android-10%2B-green?style=flat-square&logo=android" alt="Android 10+"/>
    <img src="https://img.shields.io/badge/Kotlin-1.9.22-purple?style=flat-square&logo=kotlin" alt="Kotlin"/>
    <img src="https://img.shields.io/badge/Jetpack%20Compose-1.6.0-blue?style=flat-square" alt="Compose"/>
    <img src="https://img.shields.io/badge/Material%203-Yes-orange?style=flat-square" alt="Material 3"/>
  </p>
</div>

## 📱 Sobre o Projeto

**Snap** é um aplicativo Android moderno para download e gerenciamento de vídeos, desenvolvido com as mais recentes tecnologias do ecossistema Android. Interface intuitiva, notificações inteligentes e gerenciamento completo de downloads.

### ✨ Principais Recursos

- 🎯 **Download Rápido**: Cole a URL e baixe instantaneamente
- 📊 **Gerenciamento Completo**: Histórico detalhado e estatísticas
- 🔔 **Notificações Inteligentes**: Progresso em tempo real e personalização
- 🎨 **Design Moderno**: Material Design 3 com tema claro/escuro
- 📤 **Compartilhamento Fácil**: Arquivos, estatísticas e histórico
- 🏠 **Widget**: Acesso rápido na tela inicial
- 🔒 **Privacidade**: Sem coleta de dados, armazenamento local

## Project Structure

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/snap/
│   │   │   │   ├── SnapApplication.kt           # App initialization
│   │   │   │   ├── MainActivity.kt              # Main activity
│   │   │   │   ├── data/
│   │   │   │   │   ├── models/
│   │   │   │   │   │   └── VideoModels.kt       # Data classes
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── SnapApiService.kt    # Retrofit service
│   │   │   │   │   │   └── ApiServiceFactory.kt # API configuration
│   │   │   │   │   └── repository/
│   │   │   │   │       └── VideoRepository.kt   # Data access layer
│   │   │   │   └── ui/
│   │   │   │       ├── theme/
│   │   │   │       │   └── Theme.kt             # Compose theme
│   │   │   │       ├── viewmodel/
│   │   │   │       │   ├── VideoViewModel.kt
│   │   │   │       │   ├── DownloadViewModel.kt
│   │   │   │       │   └── HistoryViewModel.kt
│   │   │   │       └── screen/
│   │   │   │           ├── InputScreen.kt       # URL input screen
│   │   │   │           ├── VideoInfoScreen.kt   # Video details screen
│   │   │   │           ├── DownloadScreen.kt    # Download progress screen
│   │   │   │           └── HistoryScreen.kt     # Download history screen
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── kotlin/com/snap/
│   │           ├── data/
│   │           │   ├── api/SnapApiServiceTest.kt
│   │           │   └── repository/VideoRepositoryTest.kt
│   │           └── ui/viewmodel/VideoViewModelTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md (this file)
```

## 🚀 Começando

### Pré-requisitos

- **Android Studio**: Hedgehog | 2023.1.1 ou superior
- **JDK**: 17 ou superior
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.2 ou superior

### Instalação

1. **Clone o repositório**

```bash
git clone https://github.com/seu-usuario/snap.git
cd snap/android
```

2. **Abra no Android Studio**

```
File → Open → Selecione a pasta 'android'
```

3. **Sincronize o Gradle**

```
File → Sync Project with Gradle Files
```

4. **Execute o app**

```bash
# Via Android Studio: Run → Run 'app'
# Via linha de comando:
./gradlew installDebug
```

## 🏗️ Arquitetura

### Stack Tecnológica

| Categoria | Tecnologia |
|-----------|------------|
| **Linguagem** | Kotlin 1.9.22 |
| **UI Framework** | Jetpack Compose 1.6.0 |
| **Design System** | Material Design 3 |
| **Arquitetura** | MVVM + Clean Architecture |
| **Injeção de Dependência** | Hilt 2.48 |
| **Banco de Dados** | Room 2.6.1 |
| **Networking** | Retrofit 2.10.0 + OkHttp |
| **Coroutines** | Kotlin Coroutines 1.7.3 |
| **Serialização** | Kotlinx Serialization 1.6.0 |
| **Preferences** | DataStore Preferences |
| **Widgets** | AppWidgetProvider |

## 🛠️ Funcionalidades

### 1. Download de Vídeos
- Input de URL com validação
- Download em segundo plano
- Notificação de progresso em tempo real
- Tratamento de erros e retry automático
- Suporte para múltiplos formatos

### 2. Histórico
- Lista completa de downloads
- Filtros por data e status
- Busca por nome/URL
- Ações em lote (deletar, compartilhar)
- Visualização de detalhes

### 3. Estatísticas
- Total de downloads
- Taxa de sucesso
- Espaço utilizado
- Vídeos por dia/semana/mês
- Gráficos interativos

### 4. Notificações
- 5 canais personalizáveis (Download, Sucesso, Erro, Info, Progresso)
- Som e vibração customizáveis
- Ações rápidas (pausar, cancelar)
- Notificação permanente durante download

### 5. Personalização
- Tema claro/escuro/automático
- Dynamic Colors (Android 12+)
- Níveis de contraste (normal, médio, alto)
- Preferências de notificação individuais

### 6. Compartilhamento
- Compartilhar arquivos baixados
- Exportar estatísticas (CSV/JSON)
- Compartilhar histórico
- Compartilhamento em lote

### 7. Widget
- Acesso rápido a downloads
- Atualização automática
- Design responsivo

## 📦 Build de Produção

### Gerar APK

```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

### Gerar AAB (Google Play)

```bash
./gradlew bundleRelease
```

**Output**: `app/build/outputs/bundle/release/app-release.aab`

### Documentação Completa

Veja os guias detalhados:
- **Build de Produção**: [RELEASE_BUILD_GUIDE.md](RELEASE_BUILD_GUIDE.md)
- **Publicação Play Store**: [PLAY_STORE_CHECKLIST.md](PLAY_STORE_CHECKLIST.md)

## 🧪 Testes

### Executar Testes Unitários

```bash
./gradlew test
```

### Executar Testes Instrumentados

```bash
./gradlew connectedAndroidTest
```

## 📊 Status do Projeto

- ✅ **Fase 1-4**: Infraestrutura, UI, Download, Database
- ✅ **Fase 5**: UI/UX Polishing
- ✅ **Fase 6**: Compartilhamento
- ✅ **Fase 7**: Widgets e Temas
- ✅ **Fase 8**: Sistema de Notificações Avançado
- ✅ **Fase 9**: Produção e Otimização
- 🚀 **Pronto para publicação na Play Store!**
## 🙏 Agradecimentos

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI Framework
- [Material Design 3](https://m3.material.io/) - Design System
- [Hilt](https://dagger.dev/hilt/) - Dependency Injection
- [Room](https://developer.android.com/training/data-storage/room) - Database
- [Retrofit](https://square.github.io/retrofit/) - HTTP Client

---

<div align="center">
  <p>Feito com ❤️ e ☕</p>
  <p>© 2024 Snap. Todos os direitos reservados.</p>
</div>
    val clearMessage: String? = null
)
```

## Networking

### Configuration

Retrofit is configured with:
- **Base URL**: `http://localhost:5000` (configurable)
- **Timeout**: 30 seconds
- **Converter**: Kotlinx Serialization
- **Interceptors**: Logging (debug mode)
- **Retry**: Connection failure retry enabled

### Error Handling

Results are wrapped in sealed class:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Examples
- `VideoRepositoryTest.kt` - Repository layer
- `SnapApiServiceTest.kt` - API service
- `VideoViewModelTest.kt` - ViewModel logic

## Build Variants

### Debug Build
- Debuggable
- Logging enabled
- No obfuscation

### Release Build
- ProGuard enabled
- Minification enabled
- Resource shrinking enabled
- Unsigned (must sign before distribution)

## Release Build

```bash
# Generate signed APK
./gradlew bundleRelease

# Or APK
./gradlew assembleRelease
```

## Gradle Configuration

### Version Properties

Edit `gradle.properties` to adjust versions:
- SDK versions (compile, min, target)
- Kotlin version
- Compose version
- Dependency versions

### Dependencies

Key dependencies:
- Core Android: `androidx.core:core-ktx`
- Compose: `androidx.compose.*`
- Network: `com.squareup.retrofit2`, `com.squareup.okhttp3`
- Database: `androidx.room:room-ktx`
- Coroutines: `org.jetbrains.kotlinx:kotlinx-coroutines-*`

## Contributing

1. Create a feature branch (`git checkout -b feature/your-feature`)
2. Commit changes (`git commit -am 'Add feature'`)
3. Push to branch (`git push origin feature/your-feature`)
4. Create Pull Request

## License

MIT License - See LICENSE file in root directory

## Support

For issues or questions:
- GitHub Issues: https://github.com/MikhaelGois/snap/issues
- Backend Issues: See main Snap project

## Roadmap

**Q1 2026**
- Complete core UI screens
- Implement offline-first data persistence
- Add pause/resume download support
- Release beta on Play Store (internal testing)

**Q2 2026**
- Advanced filtering and search
- Batch processing improvements
- Accessibility enhancements

---

## 🙏 Agradecimentos

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI Framework
- [Material Design 3](https://m3.material.io/) - Design System
- [Hilt](https://dagger.dev/hilt/) - Dependency Injection
- [Room](https://developer.android.com/training/data-storage/room) - Database
- [Retrofit](https://square.github.io/retrofit/) - HTTP Client

---

<div align="center">
  <p>Feito com ❤️ e ☕</p>
  <p>© 2024 Snap. Todos os direitos reservados.</p>
</div>
