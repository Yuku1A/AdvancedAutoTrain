# AdvancedAutoTrain
TrainCarts Addon for Automated Operation  
TrainCartsでの列車の自動運行の構築を楽にするプラグイン

## 対象の環境
Spigot(またはPaperを含むフォーク)向け

## 依存プラグイン
[TrainCarts](https://modrinth.com/plugin/traincarts)  
[SignLink](https://www.spigotmc.org/resources/signlink.39593/)

## 搭載されている機能
### 複雑な運行に不可欠な機能
CStation: 列車に紐づけたデータによって駅に停車するか、どのぐらい長く停車するか、どのように発車するかを決められる機能  
TrainPreset: スポーンした列車にその列車についている名前(セーブされた列車の識別名ではない)から、CStationやルートなどの情報を紐づける機能  
LSpawn: spawn看板に似た看板からどのような列車をどのタイミングでスポーンさせるかを任意のタイミングで決めることができる機能  
OPTimer: LSpawnや他の時間に依存する機能の基準になるタイマーの機能

## つかいかた
[CStation](docs/CStation.md)について

## 注意事項
このプラグインは、TrainCartsの`destroyAllOnShutdown`という機能が有効化されていることを前提とした仕様になっています。