
# Fabric Mod の対象Minecraftバージョンのアップデート手順

## 1. 依存関係のバージョンを確認

更新先の Minecraft version を確認する。
加えて、ユーザーに https://fabricmc.net/develop/ にアクセスさせ、各バージョン番号を取得してもらう

- Fabric Loader version
- Fabric Loom version
- Fabric API version

## 2. gradle.properties を更新

```
minecraft_version=...
loader_version=...
loom_version=...-SNAPSHOT
fabric_version=...+...
archives_base_name=LotTweaks-mc...-fabric
mod_version=...
```

**注意**:
- 2026年以降はメジャーバージョンが年（例: `26.1`）になっている
- `mod_version` を変更する場合は `src/main/java/com/github/aruma256/lottweaks/LotTweaks.java` の `VERSION` 定数も同じ値に更新すること

## 2.5. build.gradle の確認（26.1 以降）

26.1 で難読化が廃止されたため、26.1 以降への更新時は以下を確認:
- plugin id: `fabric-loom` → `net.fabricmc.fabric-loom`
- `mappings loom.officialMojangMappings()` を削除（mappings は不要）
- `modImplementation` → `implementation`（fabric-loader, fabric-api）

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
./gradlew --refresh-dependencies
./gradlew genSources vscode
```

IDEでGradleプロジェクトを再インポート（ユーザーに依頼）。

## 6. 依存関係の更新をコミット

コード修正前に、ここまでの変更（gradle.properties, fabric.mod.json, gradle-wrapper.properties）をコミットする。

## 7. コードの修正

- コンパイルエラーを確認し、API変更に対応
- Fabric公式ブログ・PR（例: 全体的なリネームは `fabric-api` の "Rename APIs to match Mojang's naming" 系PR）で破壊的変更を確認
- テストで `ItemStack` を生成する場合、26.1 以降は Bootstrap 後にコンポーネントバインドが必要:
  ```java
  HolderLookup.Provider lookup = VanillaRegistries.createLookup();
  BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(p -> p.apply());
  ```

## 8. 動作確認

ユーザーに依頼
