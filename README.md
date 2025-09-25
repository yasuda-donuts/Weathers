# Weathers

## 目的

KMPを用いて、ViewModel までコードを共有し、UIから先を Android と iOS それぞれのネイティブコードで記述。
同時期に別々のコンセプトで打ち出された Material 3 Expressive と Liquid Glass を双方試す。

## 技術スタック

*   **Kotlin Multiplatform (KMP)**: Android と iOS 間でのコード共有。今回は ViewModel まで共有
*   **Material 3 Expressive**: Android側のUIテーマと表現力豊かなコンポーネントの活用
*   **SwiftUI**: iOS固有のUI実装
*   **SKIE**: Kotlin と Swift の間を補完

## プロジェクト構成

*   `shared/`: UIに依存しない共通ビジネスロジックやデータ層を含むKotlin Multiplatformモジュール
    *   `commonMain/`: 全プラットフォームで共有されるビジネスロジック (ViewModel, Repository など)。
    *   `androidMain/`: Android固有のプラットフォームコード。
    *   `iosMain/`: iOS固有のプラットフォームコード。
*   `composeApp/`: Android用
    *   `androidMain/`: Androidの画面
*   `iosApp/`: iOS アプリケーションの Xcode プロジェクト

### Android アプリケーション

*   Android Studio の実行構成から `composeApp` を選択
*   エミュレータまたは実機で実行

<!-- <img src="/screenshots/android_screenshot.png" alt="Android App Screenshot" width="300"/> -->

### iOS アプリケーション

*  Android Studio の実行構成から `iosApp` を選択し
*  iOSシミュレータまたは実機で実行
*  or XCode でビルド (Kotlin側のビルドが Run Script 経由で走る)

<!-- <img src="/screenshots/ios_screenshot.png" alt="iOS App Screenshot" width="300"/> -->
