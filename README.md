# AdvancedAutoTrain
TrainCarts Addon for Automated Operation  
TrainCartsでの列車の自動運行の構築を楽にするプラグイン

## 依存プラグイン
[TrainCarts](https://modrinth.com/plugin/traincarts)  
[SignLink](https://www.spigotmc.org/resources/signlink.39593/)

## つかいかた
※()で囲まれた部分は括弧を含め好きな内容に置き換える  
通常のTrainCartsの看板と同じ方法で
```
[+train]
cstation
(name)
```
と書かれた看板を作る  
(name)でそのcstationに好きな名前をつける  

そして
```
cslt add (template) (section) (speed) (delay) (name)
```
とコマンドを打つ(クライアントの場合はスラッシュを先頭につける)  
これらはそれぞれ  
(template): リストの名前  
(section): 発車時に看板からどれぐらいの距離で設定された速度に達するか(メートル単位)  
(speed): 発車させるスピード  
(delay): 停車時間  
(name): 上記のcstationにつけた名前  
であり、必要に応じて変える  

このコマンドで必要な数の項目を追加したあと、  
列車のプロパティを変えるのと同じ方法で  
```/train csl (template)```
などとすると適用される  

# そのほか
他に必要なことがあれば```cslt```とだけコマンドを打つとヘルプが表示されるのでそれを見るべし
