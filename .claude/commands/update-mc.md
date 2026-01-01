
# Fabric Mod の対象Minecraftバージョンのアップデート手順

## 1. 依存関係のバージョンを確認

更新先の Minecraft version を確認する。
加えて、ユーザーに https://fabricmc.net/develop/ にアクセスさせ、各バージョン番号を取得してもらう

- Fabric Loader version
- Fabric Loom version
- Fabric API version

## 2. gradle.properties を更新

```
minecraft_version=X.XX.X
loader_version=X.XX.X
loom_version=X.XX-SNAPSHOT
fabric_version=X.XXX.X+X.XX.X
archives_base_name=LotTweaks-mcX.XX.X-fabric
```

## 3. fabric.mod.json を更新

`src/main/resources/fabric.mod.json` の `depends` セクションを更新:

```json
"depends": {
    "fabricloader": ">=X.XX.X",
    "minecraft": "~X.XX.X",
    ...
}
```

## 4. Gradle Wrapper の更新（Loom が要求する場合）

`gradle/wrapper/gradle-wrapper.properties`:

```
distributionUrl=https\://services.gradle.org/distributions/gradle-X.XX-bin.zip
```

## 5. プロジェクトをリフレッシュ

```bash
./gradlew cleanloom
./gradlew --refresh-dependencies
```

IDEでGradleプロジェクトを再インポート（ユーザーに依頼）。

## 6. コードの修正

- コンパイルエラーを確認し、API変更に対応
- Fabric公式ブログで破壊的変更を確認

## 7. 動作確認

ユーザーに依頼
