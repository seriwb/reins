[![CircleCI](https://circleci.com/gh/seriwb/reins.svg?style=shield)](https://circleci.com/gh/seriwb/reins)
[![Coverage Status](https://coveralls.io/repos/github/seriwb/reins/badge.svg)](https://coveralls.io/github/seriwb/reins)

# reins

Twitterアカウントのリストのツイートから画像を含んだツイートの情報を取得し、
定期的に画像をダウンロードして保存してくれるツールです。


## 使い方

0. [Java SE 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)をインストール。
1. [releases](https://github.com/seriwb/reins/releases/latest)からzipファイルを取得し、適当な場所に展開。
2. 必要に応じてconf/Config.groovyの値を調整。
3. コンソールからreins-0.3.1.jarがある場所まで移動し、以下のコマンドを実行。
```
java -jar reins-0.3.1.jar
```

しばらくするとdirディレクトリ配下にダウンロードファイルが保存されていきます。
もう後は画像を見ること以外にやることはありません！ラクチン！！

プログラムは挙動がおかしいと感じるまで動かしたままで大丈夫です。
ダウンロードされてないように感じた場合は、お手数ですが一度終了させて再度起動させてみてください。

終了させる場合は、コンソール上でCtrl+Cを入力してください。

データをリセットしたい場合は、dbディレクトリを削除し、再実行してください。  
**特にreinsのバージョンを上げる場合は、一度dbディレクトリを削除する必要があります。**


### 動作環境

- Javaが動作するOS（Windows、Mac）
- JRE : 1.8 以上


## ここがすごい！

- リスト毎、ユーザー毎に画像を保存するので見やすい！
- リツイートのデータも、リツイート元のユーザー名ディレクトリに画像を保存するので、
お気に入りのユーザーの開拓が捗る！
- 保存した画像ファイル名から元のつぶやきがたどれる！


----

# リリースノート

## 0.3.1版

バージョン0.3.1では以下の変更を行いました。

- 最新のTwitterAPIに対応
- reins.tweet.maxcountのデフォルト値を100に変更
- reins.loop.waittimeのデフォルト値を600(s)に変更


### 最新のTwitterAPIに対応

Twitter公式以外の画像URLの取得先変更に対応しました。


### reins.tweet.maxcountのデフォルト値を100に変更

30回ページングを行うため、100の場合は1リストにつき3000Tweetを取得します。


## 0.3版

バージョン0.3では以下の機能追加、バグ対応を行いました。

### リツイートの取得に関する動作変更

リツイートの取得有無を選択できるようになりました。  
reins.retweet.targetをfalseにすることで、リツイートを取得しなくなります。

また、リツートの画像はリツイートしたユーザのディレクトリ配下のrtディレクトリに
ユーザごとに分けて保存されるようになりました。

以前の動作にしたい場合は、reins.retweet.separateの値をfalseに変更してください。  
リストのディレクトリ直下にリツイートのユーザのディレクトリも作成されます。


### コマンド機能の追加

reinsを実行中のプロンプトで以下のコマンドが実行できるようになりました。  

- url		画像がつぶやかれたTweetのURLを取得する。（-oオプション：ブラウザでURLを表示）


#### 画像がつぶやかれたTweetのURL取得

以下のコマンドを入力することで、指定した画像がつぶやかれたTweetのURLが取得できます。

    url 画像ファイル名

さらに-oオプションを追加することで、デフォルトブラウザによるページの表示が可能です。

    url -o 画像ファイル名


### 軽微な改善

- Twitterの画像ファイルはlargeを指定して取得するように変更
- logファイルにダウンロード画像のURL情報が、INFOで出力されるように変更
- 本質でないログの出力抑制
- [Reinsのリファレンス](docs/reference.md)を作成


### バグ対応

以下のバグに対応しました。

- 複数画像があるツイートから先頭の画像しか取れていなかったのを修正


----

# プログラムガイド

## ビルド手順

Gradleのbuildタスクを実施してください。

    gradle build

#### プログラムの実行
以下のクラスに含まれるmainメソッドを実行してください。

    /reins/src/main/groovy/white/box/reins/Main.groovy

**※System.inを入れてからgradle runは正常動作しなくなったため、gradleからの実行はできません。**

### ビルド環境

- JDK : 1.8.0_112
- Groovy : 2.4.7
- Gradle : 3.4.1



## 現在の制約

### rate limit対策が不十分

sleepでwaitしているだけなので、reins.tweet.waittimeをデフォルト値よりも小さくした場合、
Twitterさんに怒られる可能性があります。
デフォルト値の場合は、1時間動かしていてもrate limitに引っかかることがなかったので、
特に理由がない限り変更しない方がよいです。

### ログ抑制がない

現状ログ抑制を行っていません。
したがって本来出すべきではないエラーも表示されています。
以下のエラーは問題ないので無視してください。

- 重複画像リンクのDB登録時に発生する一意制約エラー



# 今後の予定

- Pixivリンクの一覧化と画像取得
- タイムラインの画像取得をオプションで選択可能に。
- Instagramのデータ取得
- 画像取得対象外リストの設定
- GUIツール化


# License

MIT License
