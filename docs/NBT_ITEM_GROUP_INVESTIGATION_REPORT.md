# 1.12.2tmp ブランチ NBT対応アイテムグループ機能 調査報告書

## 調査概要

| 項目 | 内容 |
|------|------|
| 調査対象 | `1.12.2tmp` ブランチ |
| 調査日 | 2026-01-05 |
| 最終コミット | `3396e66` (Improved test cases in VersionComparatorTest) |
| 対象MC版 | 1.12.2 |

---

## 発見した主要クラス

| ファイル | 役割 |
|----------|------|
| `src/main/java/.../client/ItemState.java` | NBT込みアイテムのラッパー |
| `src/main/java/.../client/ItemGroupManager.java` | グループ管理・設定読み書き |
| `src/main/java/.../client/V2ConfigLoader.java` | 旧テキスト形式からの変換 |
| `src/main/java/.../client/DefaultGroup.java` | デフォルトグループ定義 |
| `src/main/java/.../client/selector/CircleItemSelector.java` | 円形選択UI |
| `src/main/java/.../client/selector/AbstractItemSelector.java` | セレクタ基底クラス |

---

## アーキテクチャ

```
設定ファイル (JSON)
       │
       ▼
ItemGroupManager ─── cache: Map<ItemState, List<ItemState>>
       │
       ▼
  ItemState (Item + meta + NBT をラップ)
       │
       ▼
CircleItemSelector (List<ItemStack> で表示)
```

---

## 核心クラス: ItemState

**ファイル:** `src/main/java/com/github/aruma256/lottweaks/client/ItemState.java`

NBTを含むアイテムを一意に識別するためのラッパークラス。

```java
public class ItemState {

    protected ItemStack cachedStack;

    public ItemState(ItemStack itemStack) {
        this.cachedStack = itemStack.copy();
        this.cachedStack.setCount(1);  // 個数は無視
    }

    public ItemStack toItemStack() {
        return this.cachedStack.copy();
    }

    @Override
    public boolean equals(Object obj) {
        ItemState other = (ItemState)obj;
        // NBTも含めた完全比較
        return ItemStack.areItemStacksEqual(this.cachedStack, other.cachedStack);
    }

    @Override
    public int hashCode() {
        int hash = 17 * this.cachedStack.getItem().hashCode()
                 + this.cachedStack.getItemDamage();
        if (this.cachedStack.hasTagCompound()) {
            hash += this.cachedStack.getTagCompound().hashCode();  // NBTもハッシュに含める
        }
        return hash;
    }
}
```

**設計ポイント:**
- `HashMap`のキーとして使用可能（`equals`/`hashCode`両方でNBT考慮）
- 個数は常に1に正規化（個数違いは同一視）

---

## グループ管理: ItemGroupManager

**ファイル:** `src/main/java/com/github/aruma256/lottweaks/client/ItemGroupManager.java`

### データ構造

```java
private List<List<ItemState>> groupList = new ArrayList<>();
private final Map<ItemState, List<ItemState>> cache = new HashMap<>();
```

- `groupList`: 全グループのリスト
- `cache`: 任意のItemStateからそれが属するグループへの逆引きマップ

### 設定ファイル形式 (JSON)

```json
{
  "mc_version": "1.12.x",
  "config_version": 3,
  "grouplist-0": [
    [
      {"id": "minecraft:stone"},
      {"id": "minecraft:iron_helmet"},
      {"id": "minecraft:golden_pickaxe", "nbt": "{ench:[{lvl:5s,id:32s}],display:{Name:\"Golden Pickaxe222\"}}"}
    ],
    [
      {"id": "minecraft:wool", "meta": 2},
      {"id": "minecraft:wool", "meta": 3},
      {"id": "minecraft:wool", "meta": 5}
    ]
  ],
  "grouplist-1": [...]
}
```

各アイテムは以下のフィールドを持つ:
- `id` (必須): アイテムID
- `meta` (任意): メタ値、省略時は0
- `nbt` (任意): NBT文字列

### JSON読み込みロジック

```java
private static List<List<ItemState>> readGroupFromJsonFile(JsonArray groupJsonArray) {
    for (JsonElement groupJson : groupJsonArray) {
        for (JsonElement element : groupJson.getAsJsonArray()) {
            JsonObject dict = element.getAsJsonObject();

            String itemStr = dict.get("id").getAsString();
            Item item = Item.getByNameOrId(itemStr);

            int meta = dict.has("meta") ? dict.get("meta").getAsInt() : 0;
            ItemStack itemStack = new ItemStack(item, 1, meta);

            // NBTがあれば適用
            if (dict.has("nbt")) {
                String nbtString = dict.get("nbt").getAsString();
                itemStack.setTagCompound(JsonToNBT.getTagFromJson(nbtString));
            }

            group.add(new ItemState(itemStack));
        }
    }
}
```

### グループ検索ロジック（2段階フォールバック）

```java
public List<ItemStack> getVariantsList(ItemStack itemStack) {
    ItemState itemState = new ItemState(itemStack);

    // 1. NBT込みで完全一致検索
    List<ItemState> resultsState = cache.get(itemState);

    // 2. 見つからなければNBTを除去して再検索
    if (resultsState == null) {
        itemStack = itemStack.copy();
        itemStack.setTagCompound(null);
        itemState = new ItemState(itemStack);
        resultsState = cache.get(itemState);
    }

    if (resultsState == null) return null;
    return resultsState.stream().map(e -> e.toItemStack()).collect(Collectors.toList());
}
```

**設計意図:**
- NBT付きアイテムが登録されていれば完全一致
- 未登録なら基本アイテムとして検索（後方互換性）

### 重複チェック

```java
private boolean canBeAdded(List<ItemState> group) {
    Set<ItemState> dupCheck = new HashSet<>();
    for (ItemState itemState : group) {
        if (isRegistered(itemState) || dupCheck.contains(itemState)) {
            // エラーログ出力（NBT有無で異なるメッセージ）
            return false;
        }
        dupCheck.add(itemState);
    }
    return true;
}
```

同一Item + 同一meta + 同一NBT の重複登録を禁止。

---

## 旧形式からの変換: V2ConfigLoader

**ファイル:** `src/main/java/com/github/aruma256/lottweaks/client/V2ConfigLoader.java`

旧テキスト形式 (`LotTweaks-BlockGroups.txt`) から新JSON形式への変換ローダー。

### 旧形式

```
minecraft:stone,minecraft:granite,minecraft:diorite
minecraft:wool/0,minecraft:wool/1,minecraft:wool/2
```

- カンマ区切り
- `/`でメタ値指定
- NBT非対応

### 変換処理

```java
private static ItemState createItemState(String itemStateStr) {
    String itemName;
    int meta;
    if (itemStateStr.contains("/")) {
        String[] tmp = itemStateStr.split("/");
        itemName = tmp[0];
        meta = Integer.parseInt(tmp[1]);
    } else {
        itemName = itemStateStr;
        meta = 0;
    }
    Item item = Item.getByNameOrId(itemName);
    return new ItemState(new ItemStack(item, 1, meta));
}
```

---

## デフォルトグループでのNBT活用例

**ファイル:** `src/main/java/com/github/aruma256/lottweaks/client/DefaultGroup.java`

エンチャント済み弓のグループ:

```java
l.add(toList(
    Items.BOW,
    getEnchantedStack(Items.BOW, "{ench:[{lvl:5s,id:48s}]}"),  // Power V
    getEnchantedStack(Items.BOW, "{ench:[{lvl:2s,id:49s}]}"),  // Punch II
    getEnchantedStack(Items.BOW, "{ench:[{lvl:1s,id:50s}]}"),  // Flame
    getEnchantedStack(Items.BOW, "{ench:[{lvl:1s,id:51s}]}")   // Infinity
));

private static ItemStack getEnchantedStack(Item item, String nbtStr) {
    ItemStack itemStack = new ItemStack(item);
    itemStack.setTagCompound(JsonToNBT.getTagFromJson(nbtStr));
    return itemStack;
}
```

---

## UI: CircleItemSelector

**ファイル:** `src/main/java/com/github/aruma256/lottweaks/client/selector/CircleItemSelector.java`

円形のアイテム選択UI。`List<ItemStack>`を受け取り、マウス角度で選択。

```java
public class CircleItemSelector extends AbstractItemSelector {

    public CircleItemSelector(List<ItemStack> stacks, int slot) {
        super(stacks, slot);
    }

    private ItemStack getSelectedItemStack() {
        int id = getSelectedId();
        return this.stacks.get(id);
    }

    @Override
    protected void replaceInventory() {
        ItemStack itemStack = this.getSelectedItemStack();
        mc.player.inventory.setInventorySlotContents(this.slot, itemStack);
        mc.playerController.sendSlotPacket(itemStack, 36 + this.slot);
    }
}
```

NBT付きItemStackもそのまま表示・選択可能。

---

## テストコード

### ItemStateTest

```java
// NBT付きアイテムの等価性テスト
assertEquals(
    new ItemState(createNBTstack(new ItemStack(Items.IRON_HELMET, 1, 5),
        "{ench:[{lvl:4s,id:4s}],RepairCost:1}")),
    new ItemState(createNBTstack(new ItemStack(Items.IRON_HELMET, 1, 5),
        "{ench:[{lvl:4s,id:4s}],RepairCost:1}"))
);

// NBTが異なれば不等
assertNotEquals(
    new ItemState(createNBTstack(..., "{ench:[{lvl:4s,id:4s}]}")),
    new ItemState(createNBTstack(..., "{ench:[{lvl:4s,id:3s}]}"))  // id違い
);

// NBT有無で不等
assertNotEquals(
    new ItemState(new ItemStack(Items.IRON_HELMET)),
    new ItemState(createNBTstack(new ItemStack(Items.IRON_HELMET), "{ench:[...]}"))
);
```

### ItemGroupManagerTest

```java
// NBT付きアイテムの登録確認
assertTrue(instance.isRegistered(createNBTstack(
    new ItemStack(Items.GOLDEN_PICKAXE),
    "{ench:[{lvl:5s,id:32s}],display:{Name:\"Golden Pickaxe222\"}}"
)));

// NBTが異なれば未登録
assertFalse(instance.isRegistered(createNBTstack(
    new ItemStack(Items.GOLDEN_PICKAXE),
    "{ench:[{lvl:5s,id:32s}]}"  // display無し
)));
```

---

## まとめ

| 観点 | 実装内容 |
|------|----------|
| **識別方式** | Item + meta + NBT の3要素で一意識別 |
| **データ構造** | `ItemState`ラッパー + `HashMap`キャッシュ |
| **設定形式** | JSON（`id`, `meta`, `nbt`フィールド） |
| **検索戦略** | NBT完全一致 → NBT無視のフォールバック |
| **後方互換** | 旧テキスト形式からの自動変換 |
| **UI** | `List<ItemStack>`をそのまま表示（NBT透過） |

この設計により、エンチャント済みアイテムや名前付きアイテムなど、NBTで区別されるアイテムもグループとして管理可能になっていた。
