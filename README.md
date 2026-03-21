
# Shogi Engine OEX

チェス用のOEX（Open Exchange）エンジンテンプレートをベースに、将棋エンジン向けにカスタマイズしたプロジェクトです。

## 概要
本プロジェクトは、Android OS上で動作する将棋エンジン（USIプロトコル対応）を、外部アプリから利用可能な「OEXエンジン」としてパッケージングするためのテンプレートです。

## エンジンの作成・ビルド手順

### 1. エンジンバイナリのビルド
NDK（Native Development Kit）を使用して、将棋エンジンのソースコードをAndroid向けにビルドしてください。

### 2. ライブラリファイルの配置
ビルドしたバイナリを `app/src/main/jniLibs` 配下に配置します。
* **命名規則:** ファイル名は `lib[エンジン名].so` としてください。
* **ディレクトリ構造の例:**
    * 64bit Android実機向け: `arm64-v8a/libxxx.so`
    * エミュレータ向け: `x86_64/libxxx.so`

### 3. 評価関数・アセットの配置
評価関数ファイルや定跡データなどは、`app/src/main/assets/` 内の任意のフォルダに配置します。
* 例: `app/src/main/assets/engine_data/`

### 4. 設定ファイル（enginelist.xml）の編集
`app/src/main/res/xml/enginelist.xml` を開き、エンジンの情報を定義します。

```xml
<engine
    name="YaneuraOu 7.5 + Suisho5" 
    filename="libyaneuraou.so"
    target="arm64-v8a|x86_64" 
    assets="engine_data"/>
```
* `name`: アプリ上に表示されるエンジン名
* `filename`: 手順2で命名した `.so` ファイル名
* `target`: 対応するCPUアーキテクチャ
* `assets`: 手順3で作成したフォルダ名

### 5. パッケージ名の変更
デフォルトのパッケージ名（`com.siganus.oexengine`）を、自身の固有の名前に変更してください。
以下の箇所の書き換えが必要です。

* `app/build.gradle.kts` (`applicationId`)
* `AndroidManifest.xml`
* `MainActivity.kt`
* `provider.kt`
* ディレクトリ構造（`src/main/java/com/siganus/oexengine` を新しいパッケージ名に合わせて移動）

### 6. アプリの調整
必要に応じて、アプリ名（`strings.xml`）、アイコン、レイアウトなどのリソースを調整してください。

### 7. APKの作成・署名
Android Studioのメニューからビルドを行います。
1.  **Build** ＞ **Generate Signed Bundle / APK...** を選択
2.  手順に従って署名済みのAPKを作成します。
    > **Note:** 配付する場合は製品版として署名（Releaseビルド）を行う必要があります。



