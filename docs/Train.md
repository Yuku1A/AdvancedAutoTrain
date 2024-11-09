# 新しい列車を設定する流れ
すでに[ワールド内で必要な設備](SettingInWorld.md)の設置が終わっているものとする  
また、[OPTimer](OPTimer.md)、[LSpawn](LSpawn.md)の準備も済ませてあるものとする
1. [reditコマンド](commands/redit.md)やTrainCartsの機能でRouteを設定して名前をつけて保存する
2. [csltコマンド](commands/cslt.md)で列車がどの駅に停車し、どのように発車するのかの内容を入力する
3. [tpresetコマンド](commands/tpreset.md)で列車の名前に対して上記の内容を紐づける
4. 対象の編成を安全な場所にスポーンさせ、列車自体に前の手順で決めた名前をつけ、分かりやすい名前で保存(保存時の名前ではなく、列車の名前を付与する)
5. [lspnコマンド](commands/lspn.md)で、列車の設定を追加する
6. 完了