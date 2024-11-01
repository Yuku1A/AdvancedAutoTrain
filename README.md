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

### 旅客案内用機能
ArrivalList: (trigger看板ではなく)CStationを基準として事前に記録したデータを元にSignLinkと連動して看板に列車情報を表示する機能

### データ管理用機能
TrainRecord: 列車ごとにCStationの発車タイミングなどを記録する機能  
AATDump: TrainRecordやその他のデータを外部のソフトウェアで解析できるように出力する機能

## つかいかた
[CStation](docs/CStation.md)について

## コマンド一覧
`<command> help`とするとそれぞれコマンドのサブコマンド一覧が表示されます  
[cslt](docs/commands/cslt.md): どのCStationにとまるのかなどをまとめたリストを列車とは独立して設定するコマンド(列車には他の機能を使用して紐づけられる)    
[optimer](docs/commands/optimer.md): OPTimerの設定をするコマンド     
[lspn](docs/commands/lspn.md): LSpawnの情報を設定するコマンド  
[tpreset](docs/commands/tpreset.md): TrainPresetを設定するために使用するコマンド    
[trec](docs/commands/trec.md): TrainRecordの記録や管理用に使用するコマンド   
[ar](docs/commands/ar.md): CStationごとに表示される列車の情報を制御する(直接使用することは推奨されません)  
[tal](docs/commands/tal.md): TrainRecordと列車単位で事前設定された情報に基づいてArrivalListを自動設定するために使用するコマンド     
[aatdump](docs/commands/aatdump.md): このプラグインに関するデータを外部に出力して解析できるようにする   
[trl](docs/commands/trl.md): aatdumpの使用時にOPTimerごとに出力する列車の情報を選択してデータの肥大化を軽減する     
[cstationcache](docs/commands/cstationcache.md): CStationのサジェスト元のデータを編集するコマンド(0.12.0より前に使い始めた人向け)   
[redit](docs/commands/redit): TrainCarts内で保存されるRouteの情報を直接編集するコマンド

## 注意事項
このプラグインは、TrainCartsの`destroyAllOnShutdown`という機能が有効化されていることを前提とした仕様になっています。