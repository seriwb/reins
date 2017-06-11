# list_${listId}

リストID単位で作成する、Twitter情報を格納するテーブル。
（リストに含まれるTwitter情報を表しているので、ちょっと名前がいけていない）

| name | type | description |
| :--- | :--- | :---|
| id | bigint unsigned | primary key |
| screenName | varchar2(50) | ユーザのTwitter ID |
| retweetUser | varchar2(50) | Retweetしたユーザ。いなければnull |
| counterStatus | integer | ダウンロード試行回数。デフォルト0 |
| statusId | bigint unsigned | TweetのstatusId |
| attribute | varchar2(10) | 画像URLの属性。twitter, gif, pixivなどで分かれる |

```sql
create table if not exists list_${listId} (
id bigint unsigned auto_increment not null primary key,
screenName varchar2(50),
retweetUser varchar2(50),
counterStatus integer,
statusId bigint unsigned,
attribute varchar2(10))
```

ユーザのURLは
https://twitter.com/${screenName}/status/${statusId}


# twitter_${screenName}

ユーザごとの画像とURL情報のテーブル

| name | type | description |
| :--- | :--- | :---|
| statusId | bigint unsigned | TweetのstatusId(primary key) |
| imageName | varchar2(75) | 画像ファイル名(ユニークキー) |
| imageUrl | varchar2(300) | 画像のURL |
| attribute | varchar2(10) | 画像URLの属性。twitter, gif, pixivなどで分かれる |
| twitterUrl | varchar2(300) | Twitter URL |
| tweetDate | datetime | Tweetの日付 |

```sql
create table if not exists twitter_${screenName} (
    id bigint unsigned not null primary key,
    statusId bigint unsigned not null,
    imageName varchar2(75) unique,
    imageUrl varchar2(300) unique,
    attribute varchar2(10),
    twitterUrl varchar2(300),
    tweetDate datetime)
```