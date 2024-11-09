# ワールド内に必要な設備
## CStation看板
停車させたい場所や列車のタイミングを計測したい場所に、以下のような看板をTrainCartsの看板として設置  
(name)は識別しやすい名前(英数字を強く推奨)にする
```
[+train]
cstation
(name)
```
CStationのみならこれを駅に配備するだけでよい

## LSpawn看板
列車をスポーンさせたい場所(TrainCartsに置いてspawn看板を設置するような場所)に、以下のような看板をTrainCartsの看板として設置する  
lspawnの行の(speed)は、spawn看板に使用するものと同じ  
事前に簡単なspawn看板で検証しておくことを推奨する  
(listname)はlspnコマンドで設定する内容と同じものにする
```
[+train]
lspawn (speed)
(listname)
```

## 他に必要なTrainCartsの看板
[Destination](https://wiki.traincarts.net/p/TrainCarts/Signs/Destination)