
# Reinsの機能

## Twitter画像の自動ダウンロード
認証したTwitterアカウントのリスト内のツイートから、画像を含んだツイートの情報を取得し、
定期的に画像をダウンロードして保存します。

## OAuthでのクライアント認証に対応
Web認証実施後に表示されるPINをプログラムに渡すことでreinsの利用が可能です。

## ログ出力対応
logフォルダ配下にLTSV形式のログが出力されます。

## ネットワーク接続エラーへの対応（0.2.1版）
Twitterから情報取得時にネットワークエラーが発生した場合、15分後に再度接続処理を行います。



# 使い方

## 動作環境

- Javaが動作するOS（Windows、Mac）
- JRE : 1.8 以上


## 実行方法

0. [Java SE 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)をインストール
1. [releases](https://github.com/seriwb/reins/releases/latest)からzipファイルを取得し、適当な場所に展開
2. 必要に応じてconf/config.txtの値を調整
3. 画像取得対象のリストを選びたい場合は、conf/allow.txtにリスト名を追加
4. 画像取得対象外のリストを設定したい場合は、conf/deny.txtにリスト名を追加
5. コンソールからreins-0.3.4.jarがある場所まで移動し、以下のコマンドを実行
```
java -jar reins-0.3.4.jar
```


## 設定の変更方法

Reinsの動作設定は```conf/config.txt```を編集することで変更が可能です。

以下の内容が変更可能です。

- 画像の保存場所：reins.image.dir
- Tweetの1回の取得数（MAX:100）：reins.tweet.maxcount
- 繰り返しのWait間隔ベース（ms）：reins.loop.waittime
- Retweetの取得：reins.retweet.target
- Retweetのディレクトリを別ける：reins.retweet.separate
- タイムラインの取得：reins.timeline.target
- リストの取得：reins.list.target


### 設定項目についての説明

```
// 画像の保管場所
reins.image.dir = "./dir"

// Tweetの1回の取得数（MAX:100）
reins.tweet.maxcount = 100

// 繰り返しのWait間隔ベース（s）：画像取得はこの時間の半分
reins.loop.waittime = 600

// Retweetの取得（取得する：true/取得しない：false）
reins.retweet.target = true

// Retweetのディレクトリを分離（分離する：true/分離しない：false）
reins.retweet.separate = true

// タイムラインの画像取得（取得する：true/取得しない：false）
reins.timeline.target = true

// リストの画像取得（取得する：true/取得しない：false）
reins.list.target = true
```

#### 1. reins.image.dir

画像ファイルの保存先のディレクトリを指定できます。  
デフォルトではreinsを実行したディレクトリ配下のdirディレクトリに作成されます。


#### 2. reins.tweet.maxcount

一度に取得するTweetの数です。  
特に理由がない限り変更の必要はありません。


#### 3. reins.loop.waittime

reinsがTwitterから情報を取得する間隔の基準値になります。  
特に理由がない限り変更の必要はありません。

TwitterのRate Limitによく引っかかってしまう場合は、デフォルト値（600s=10m）よりも値を増やしてみてください。


#### 4. reins.retweet.target

リツイートを取得するかどうかを選択できます。  
trueでリツイートを取得し、falseで取得しなくなります。


#### 5. reins.retweet.separate

リツイートをリツイートしたユーザ毎に分けるかどうかのオプションです。

- trueにした場合は、リツイートしたユーザのディレクトリ配下にrtディレクトリが作成され、
その配下にリツイートの画像がユーザ毎に保存されます。
- falseにした場合は、リツイートを区別せずにリストのディレクトリ配下にユーザ毎に画像が保存されます。


#### 6. reins.timeline.target

認証ユーザのタイムラインから画像を取得するかどうかを選択できます。  
trueでタイムラインから取得し、falseで取得しなくなります。


#### 7. reins.list.target

認証ユーザが持つ各リストから画像を取得するかどうかを選択できます。  
trueで各リストから取得し、falseで取得しなくなります。

画像の取得先がタイムラインだけでいいような場合は、本オプションをfalseに設定してください。


## reinsコマンド

reinsを実行中のプロンプトで各種コマンドが実行できます。

- url		画像がつぶやかれたTweetのURLを取得する。（-oオプション：ブラウザでURLを表示）


#### 画像がつぶやかれたTweetのURL取得

以下のコマンドを入力することで、指定した画像がつぶやかれたTweetのURLが取得できます。

    url 画像ファイル名

さらに-oオプションを追加することで、デフォルトブラウザによるページの表示が可能です。

    url -o 画像ファイル名
