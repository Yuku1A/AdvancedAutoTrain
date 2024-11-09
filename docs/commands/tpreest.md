# tpreset コマンド

TrainPresetの管理をするコマンド

### `tpreset add <trainname> [cstationlist] [route] [tags...]`
TrainPresetを追加するコマンド
\<trainname>は列車名(列車自体についてる`train list`で出てくる名前)、  
\[cstationlist]はCStationListTemplateで、[csltコマンド](docs/commands/cslt.md)で設定したものの名前、\[route]はTrainCarts側で保存されているRoute、\[tags...]は空白を区切りとしてタグを設定する  
