# LotTweaks Fabric リファクタリング計画

## 現状分析

### コードベース概要
- 総行数: 約2,400行
- クラス数: 25
- テストカバレッジ: 極めて低い（3つの簡易テストのみ）

### 主要な問題点

#### 1. RotationHelper.java (425行) - 最大の問題
※機能名を「Rotation」から「Palette」に変更予定（ブロックパレットから選ぶUIとして直感的）

複数の責務が混在:
- デフォルトグループ定義（ハードコード文字列配列 ~150行）
- ファイルI/O（読み込み/書き込み）
- 設定パース処理
- バリデーション処理
- グループチェーン管理（循環リンク構造）
- 色バリエーション生成ユーティリティ

#### 2. 設定のハードコード（本リファクタリング対象外）
`LotTweaks.CONFIG` クラス内の設定値がソースコードに直接記述されている。
→ 設定の外部化は別途対応予定のため、本リファクタリングでは対象外。

#### 3. テストの不足
- 現在のテスト: 基本的なアサーションのみ
- RotationHelperのパース/バリデーションロジックのテストなし
- パケットの検証ロジックのテストなし

#### 4. 命名の不統一
- `LT` プレフィックス（意味が不明瞭）
- `ExPickKey` → "Extended Pick" の略だが分かりにくい
- `renderer/` と `render/` の不統一

#### 5. その他
- `ExPickKey.LeftClickBlock` 内部クラス（Forge互換性のためのワークアラウンド）
- イベントシステムの静的リスナーリスト（スレッドセーフではない）
- マジックナンバー（50000, 12345, 5 ticks など）


## 目標アーキテクチャ

### 設計原則
1. **単一責任**: 各クラスは1つの明確な目的を持つ
2. **テスタビリティ**: Fabric Loader JUnit を活用（Minecraft APIへの依存は許容し、無理な抽象化はしない）
3. **明確な境界**: データモデル、I/O、ビジネスロジックの分離

### 目標パッケージ構造

```
com.github.aruma256.lottweaks/
├── LotTweaks.java                    # エントリーポイント + CONFIG（設定外部化は別途対応）
├── network/
│   ├── LTPacketHandler.java          # 現状維持（構造は適切）
│   └── ServerConnectionListener.java
└── reach/
    └── ReachDistanceManager.java     # AdjustRangeHelper から改名（BLOCK_INTERACTION_RANGEのModifierを管理）

(client)
com.github.aruma256.lottweaks/
├── LotTweaksClient.java              # エントリーポイントのみ
├── palette/                          # 【新規】RotationHelper を分割、機能名を Palette に変更
│   ├── ItemPalette.java              # コアパレットロジック（テスト可能）
│   ├── PaletteGroup.java             # Enum (PRIMARY/SECONDARY)
│   ├── ItemGroupParser.java          # パースロジック（純粋、テスト可能）
│   └── PaletteConfigManager.java     # ファイルI/O + リソースからのデフォルトコピー
# リソースファイル（src/client/resources/assets/lottweaks/）:
#   - default-block-groups.txt        # PRIMARY デフォルト
#   - default-block-groups2.txt       # SECONDARY デフォルト
├── keybinding/                       # keys/ から改名
│   ├── KeyBase.java                  # LTKeyBase から改名
│   ├── ItemCycleKeyBase.java         # ItemSelectKeyBase から改名
│   ├── SmartPickKey.java             # ExPickKey から改名（バニラpickBlockの拡張版）
│   ├── PaletteKey.java               # RotateKey から改名（機能名変更）
│   ├── ReplaceBlockKey.java          # ReplaceKey から改名
│   └── ReachExtensionKey.java        # AdjustRangeKey から改名
├── event/                            # 現状維持
├── render/                           # renderer/ から改名
│   ├── ItemStackRenderer.java        # LTRenderer から改名
│   ├── HudTextRenderer.java          # LTTextRenderer から改名
│   └── SelectionBoxRenderer.java
├── mixin/client/                     # 現状維持
├── command/
│   └── LotTweaksCommand.java
└── network/
    └── ClientPacketSender.java       # LTPacketHandlerClient から改名
```


## リファクタリングステップ

各ステップは1セッションで完了可能な規模に分割。

---

### Step 1: RotationHelper の分割と Palette への改名（TDDアプローチ）
**状態: 未着手**

RotationHelper.java (425行) を `palette/` パッケージに分割。
**TDD**: テスト可能な部分は「テストを先に書く → 実装」の順序で進める。

#### 1-1. PaletteGroup.java + テスト
- `Group` enumを独立ファイルに移動
- テスト: enum値の存在確認（軽量）

#### 1-2. ItemGroupParser.java + テスト（TDD）
**テストを先に書く:**
- `ItemGroupParserTest.java`
  - 正常なグループ定義のパース
  - 重複アイテムの検出
  - 無効なアイテムIDの処理
  - グループサイズ1以下の検出
  - コメント行・空行のスキップ

**実装:**
- `loadItemGroupFromStrArray()` メソッド
- `warnGroupConfigErrors()` メソッド
- バリデーションロジック
- Minecraft API依存は許容（Fabric Loader JUnitを活用してテスト）

#### 1-3. ItemPalette.java + テスト（TDD）
**テストを先に書く:**
- `ItemPaletteTest.java`
  - `canCycle()` の動作確認
  - `getAllCycleItems()` の循環リスト生成
  - 空/null入力の処理

**実装:**
- `ITEM_CHAIN_PRIMARY`, `ITEM_CHAIN_SECONDARY` データ構造
- `canCycle()` メソッド（旧 `canRotate`）
- `getAllCycleItems()` メソッド（旧 `getAllRotateResult`）
- 公開API

#### 1-4. デフォルト設定のリソースファイル化
**DefaultItemGroups.java は作成しない。代わりにリソースファイルを使用:**
- `src/client/resources/assets/lottweaks/default-block-groups.txt`
- `src/client/resources/assets/lottweaks/default-block-groups2.txt`
- `toColorVariationsStr()`, `toSameColorsStr()` は不要になる（展開済みのテキスト）

**作成方法:** 既存ファイルをそのままコピー
```
run/config/LotTweaks-BlockGroups.txt  → src/client/resources/assets/lottweaks/default-block-groups.txt
run/config/LotTweaks-BlockGroups2.txt → src/client/resources/assets/lottweaks/default-block-groups2.txt
```

#### 1-5. PaletteConfigManager.java
- `loadFromFile()` メソッド
- `writeToFile()` メソッド
- `loadFile()` メソッド（エンコーディングフォールバック）
- `copyDefaultFromResources()` メソッド（リソースからconfigへコピー）
- ファイルI/Oはテスト困難なため、ロジックを最小限に保つ

**成果物:**
- 4つのクラス + 2つのリソースファイル
- 責務の明確な分離
- 10-15個のユニットテスト
- パレットロジックの信頼性向上

---

### Step 2: キーバインディングのリネームと整理
**状態: 未着手**

1. パッケージ名変更: `keys/` → `keybinding/`
2. クラス名変更:
   - `LTKeyBase` → `KeyBase`
   - `ItemSelectKeyBase` → `ItemCycleKeyBase`
   - `ExPickKey` → `SmartPickKey`（バニラpickBlockの拡張版）
   - `RotateKey` → `PaletteKey`（機能名変更に伴う）
   - `ReplaceKey` → `ReplaceBlockKey`
   - `AdjustRangeKey` → `ReachExtensionKey`

3. `SmartPickKey.LeftClickBlock` 内部クラスの削除
   - Fabric APIを直接使用するようリファクタリング

**成果物:**
- 命名の明確化
- 不要なワークアラウンドの削除

---

### Step 3: レンダラーのリネーム
**状態: 未着手**

1. パッケージ名変更: `renderer/` → `render/`
2. クラス名変更:
   - `LTRenderer` → `ItemStackRenderer`
   - `LTTextRenderer` → `HudTextRenderer`
   - `SelectionBoxRenderer` → そのまま

3. マジックナンバーの定数化
   - アニメーションタイミング値に名前を付ける

**成果物:**
- 命名の明確化
- コードの可読性向上

---

### Step 4: パケットハンドラーの整理（オプション）
**状態: 未着手**

現状の構造は許容範囲だが、以下を検討:

1. 各メッセージタイプを別ファイルに分離
   - `packet/ReplacePacket.java`
   - `packet/AdjustRangePacket.java`
   - `packet/HelloPacket.java`

2. バリデーションロジックを専用クラスに抽出

**成果物:**
- より明確な構造
- 各パケットタイプの独立性

---

### Step 5: 最終レビューとクリーンアップ
**状態: 未着手**

1. 未使用コードの削除
2. Javadocコメントの追加（公開APIのみ）
3. `ClientChatEvent` の削除（未使用）
4. コンパイル警告の解消
5. 全テストの実行と確認

**成果物:**
- クリーンなコードベース
- すべてのテストがパス


## 進捗トラッキング

| Step | 説明 | 状態 | 完了日 |
|------|------|------|--------|
| 0 | バージョン 2.3.7 に更新 | ✅ 完了 | 2026-01-03 |
| 1 | RotationHelper → palette/ 分割 + テスト（TDD） | ✅ 完了 | 2026-01-04 |
| 2 | キーバインディングのリネーム | 未着手 | - |
| 3 | レンダラーのリネーム | 未着手 | - |
| 4 | パケットハンドラーの整理（オプション） | 未着手 | - |
| 5 | 最終レビュー | 未着手 | - |


## 参考情報

### Fabric 自動テスト
- **Fabric Loader JUnit**: 導入済み。Minecraftレジストリへのアクセスが必要なテストに使用。 https://raw.githubusercontent.com/FabricMC/fabric-docs/refs/heads/main/develop/automatic-testing.md
- **Game Test**: `net.fabricmc.fabric.api.client.gametest.v1` - UIテストに適している可能性があるが、
  本リファクタリングでは純粋ロジックのユニットテストを優先。

### 注意事項
- 各ステップ完了後、必ず `./gradlew build` でビルド確認
- 各ステップ完了後、必ず `./gradlew test` でテスト実行
- Minecraft環境での動作確認も適宜実施
