# CStation機能の使い方

[CStation看板の設置](#cstation看板の設置)  
[列車に設定するデータの準備](#列車に設定するデータの準備)

### CStation看板の設置
※()で囲まれた部分は括弧を含め好きな内容に置き換える  
通常のTrainCartsの看板と同じ方法で
```
[+train]
cstation
(name)
```
と書かれた看板を作る  
(name)でそのcstationに好きな名前をつける

### 列車に設定するデータの準備
```
cslt add (template) (acceleration) (speed) (delay) (name) (announce)
```
とコマンドを打つ(クライアントの場合はスラッシュを先頭につける)  
これらはそれぞれ  
(template): リストの名前  
(acceleration): 発車時に看板からどれぐらいの距離で設定された速度に達するか(メートル単位)、もしくはTrainCartsで使用できる表記による加速度  
(speed): 発車させるスピード  
(delay): 停車時間  
(name): 上記のcstationにつけた名前  
(announce): 列車が駅に停車する際に乗客のログに流す文章(なくてもいい)  
であり、必要に応じて変える

このコマンドで情報を追加できる

### 列車に直接適用する
列車のプロパティを変えるのと同じ方法で  
```/train csl (template)```
などとすると適用される  
これだけではなく、[TrainPreset](docs/TrainPreset.md)に設定するなどの方法もある

# そのほか
他に必要なことがあれば```cslt```とだけコマンドを打つとヘルプが表示されるのでそれを見るべし