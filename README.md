# _KiraDB_

_Description: KiraDB is a simple, lightweight NoSQL-style embedded database Java API_

## Installing

If using Maven, add the repository and dependencies to your pom file:


	  <repositories>
	    <repository>
	      <id>bdt</id>
	      <name>BDT/KiraDb</name>
	      <url>http://mrblog.github.com/KiraDB/maven2</url>
	    </repository>
	  </repositories>

Dependencies:

	<dependency>
	    <groupId>com.bdt</groupId>
	    <artifactId>kira-db</artifactId>
	    <version>1.0</version>
	</dependency>

If you want to compile it yourself, here's how:

    $ git clone https://github.com/mrblog/KiraDB.git
    $ cd KiraDb
    $ mvn install       # Requires maven, download from http://maven.apache.org/download.html

The pre-built jar is available at: 

*   [kira-db-1.0.jar](http://mrblog.github.com/KiraDB/maven2/com/bdt/kira-db/1.0/kira-db-1.0.jar)

You'll need to include versions of the dependencies yourself. You will need the following libraries (See the pom.xml for the more details):

* commons-io-1.4.jar
* commons-lang-2.4.jar
* ehcache-core-2.4.3.jar - optional, required for default caching
* jets3t-0.7.4.jar - only needed if using Amazon S3 backing store
* lucene-core-3.0.3.jar
* lucene-queries-3.0.3.jar
* slf4j-api-1.6.1.jar
* slf4j-jdk14-1.6.1.jar
* xpp3_min-1.1.4c.jar
* xstream-1.3.1.jar

You can view the javadocs for this project at: http://mrblog.github.com/KiraDB/apidocs/

## Getting Started

Storing data with KiraDB is built around a `Record` interface. Each `Record` class describes  the data model, as a POJO (Plain old Java Object) implementing the `Record` interface.  

For example, let's say you're tracking high scores for a game. A `GameScore` class as follows implements the data model.

```

	public class GameScore {
	
		private String gameNumber;
		private int score;
		private String playerName;
		private String team;
		private Boolean cheatMode;
		
		public GameScore() {
		}

		public GameScore(String gameNumber, int score, String playerName, Boolean cheatMode) {
			setGameNumber(gameNumber);
			setScore(score);
			setPlayerName(playerName);
			setCheatMode(cheatMode);
		}
		public void setGameNumber(String gameNumber) {
			this.gameNumber = gameNumber;
		}
	
		public String getGameNumber() {
			return gameNumber;
		}
	
		public void setScore(int score) {
			this.score = score;
		}
	
		public int getScore() {
			return score;
		}
	
		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}
	
		public String getPlayerName() {
			return playerName;
		}

		public void setTeam(String team) {
			this.team = team;
		}

		public String getTeam() {
			return team;
		}

		public void setCheatMode(Boolean cheatMode) {
			this.cheatMode = cheatMode;
		}
	
		public Boolean getCheatMode() {
			return cheatMode;
		}
	
	}

```

In order to use this class with KiraDB, it would be augmented as follows to implement 
the `Record` interface:

```

	public class GameScore implements Record {
	
		private static final String RECORD_NAME = "scores";
		private static final String PRIMARY_KEY = "game";
		public static final String NAME = "name";
		public static final String SCORE = "score";
		public static final String TEAM = "team";

		...
	
		@Override
		public RecordDescriptor descriptor() {
			RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
			dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, getGameNumber()));
			dr.addField(new Field(NAME, FieldType.STRING, getPlayerName()));
			dr.addField(new Field(SCORE, FieldType.NUMBER, getScore()));
			dr.addField(new Field(TEAM, FieldType.STRING, getTeam()));
			dr.setStoreMode(RecordDescriptor.STORE_MODE_INDEX);
			return dr;
		}
	
		@Override
		public String getRecordName() {
			return RECORD_NAME;
		}
	
		@Override
		public String getPrimaryKeyName() {
			return PRIMARY_KEY;
		}
	
	}
```

These methods inform KiraDB of the data model associated with the Object Class.

1. Define the name `RECORD_NAME` analogous to a SQL table name
2. Define the `PRIMARY_KEY` used for the record.
3. Define any additional index fields that may be needed for query/sort
4. Define the storage mode for the Object Class

Keys and Field names must be alphanumeric strings. Field values can be strings, numbers, dates, or full-text fields (more below). The Object itself can contain any kind of Java Object, Strings, Dates, Maps, Arrays, etc.

Each `Record` is an instance of a specific subclass with a class name and record name  that you can use to distinguish different sorts of data. For example, we call the high score object a `GameScore` class with the record name `"scores"` (`RECORD_NAME`). 

To create a new subclass, implement the `Record` interface. KiraDB will return instances of the new class for any Object with the specified record name

## Initializing KiraDB

```

	KiraDb db = new KiraDb(new File("KiraDBIndex"));

```


## Saving Objects

Let's say you want to save the GameScore described above to KiraDB. 

```

	GameScore p1 = new GameScore("Game157", 1337, "Kevin Blake", false);
	db.storeObject(p1);
```


## Retrieving Objects

If you have the primary key value, you can retrieve the Object using the `retrieveObjectByPrimaryKey` method:

```

	GameScore theScore = (GameScore) db.retrieveObjectByPrimaryKey("Game157");

```

## Queries

We've already seen how you can retrieve a single Object from KiraDB. There are many other ways to retrieve data with the `executeQuery` method - you can retrieve many objects at once, put conditions on the objects you wish to retrieve, and more.

### Basic Queries

The general approach is to create a query, put conditions on it, and then retrieve a `List` of matching Objects using `executeQuery`. For example, to retrieve the scores associated with a particular team, by constraining the value for a key.

```

        List<GameScore> qResults = db.executeQuery(new GameScore(), GameScore.TEAM, "shaggy", 10, 0, GameScore.SCORE, true);


```

This returns scores belonging to team "shaggy" returning 10 results at a time, skipping 0 results (i.e. starting at 0), sorting by SCORE in reverse sort order (highest score first).

### Counting Records

If you just need to count how many objects match a query, but you do not need to retrieve all the objects that match, you can use getTotalHits(). For example, to count how many games have been played by a particular player:

```
        List<GameScore> qResults = db.executeQuery(new GameScore(), GameScore.NAME, "Kevin Blake", 1, 0, GameScore.SCORE, true);
	int count = db.getTotalHits();

```

### Paging Through Results

To page through query results, use the `hitsPerPage` and `skipDocs` parameters:

```

       List<GameScore> qResults = db.executeQuery(new GameScore(), GameScore.TEAM, "shaggy", PER_PAGE, (page-1)*PER_PAGE, GameScore.SCORE, true);

```

where `PER_PAGE` is the number of objects per page and `page` in this case is the page number (starting from 1).

### Primary Keys

Primary Keys in KiraDB must be `String` fields (`FieldType.STRING`) which are simple case-sensitive string values. The Primary Key is unique across all records of this class.

### Fields

The `GameScore` example adds a field for the user's score. This field is to be treated as a `NUMBER` by KiraDB. This effects searching and sorting based on the field.

### Record, Key, and Field Names

Record, Primary Key, and Field Names are case-sensitive and must consist of letters and digits only.

## Object Store

KiraDB can operate in multiple modes:

1. `STORE_MODE_NONE` Indexing (supporting searching and sorting operations) only (no storage of the full object)
2. `STORE_MODE_INDEX ` Storing Objects in the KiraDB index
3. `STORE_MODE_BACKING ` Storing Objects in a separate Backing Store (`BackingStore`)

More details on these modes is provided in the sections that follow.

### `STORE_MODE_NONE` Indexing only

In this mode, KiraDB is only performing indexing (searching and sorting) for the user. Object storage and retrieval is entirely the responsibility of the user. Record objects returned by KiraDB contain only the primary key and the fields associated with the Record class.

### `STORE_MODE_INDEX` Objects stored in the KiraDB index

In this mode, KiraDB will store user objects with the index. KiraDB will return the full object class implementing the `Record` interface as shown in the examples above for the GameScore object class. However, in this mode, there is no authoritative data store other than the index, which reduces redundancy. If the index should become corrupt, user data could be lost.

### `STORE_MODE_BACKING` Using a Backing Store

In this mode, KiraDB works with a connected `BackingStore` for the authoritative  object repository. If the index should become corrupt, it could be rebuilt from the backing store.

KiraDB includes two included backing store implementations, a simple File-based object store (`FileBackingStore`) and an Amazon S3 based backing store (`S3BackingStore`)

Users can provide their own custom backing store by implementing their own `BackingStore` class.

Convenience classes `S3KiraDB` and `FileSystemKiraDb` are provided for constructing a Core KiraDB instance configured with the corresponding backing store. Refer to their Javadocs for information on their use.

## Full-text Searching

KiraDb supports full-text searching including an English-language stemmer. Let's say you have a field in your `Record` class for a title. Add this field as a `FULLTEXT` field and you can then perform full-text searches on that field:

	dr.addField(new Field(TITLE, FieldType.FULLTEXT, getTitle()));

Perform a full-text search on this field with the `executeQuery()` method:

       List<MyClass> qResults = db.executeQuery(new MyClass(), MyClass.TITLE, "logic", null, false);

## More Like This

KiraDb provides a feature to locate Objects that are similar to a specified test string. This can be used to identify other Objects that are similar to a given Object. For example:

        String[] fieldNames = { TextDocument.BODY };
        List<String> matches = db.relatedObjects(new TextDocument(), doc.getBody(), fieldNames, 5, doc.getId());

This returns a list of up to 5 matching document Id's (the primary field for the `TextDocument` Object class), excluding the input "reference" object (`doc`).

You can also find all documents that match any given reference string, as in:

        String[] fieldNames = { TextDocument.BODY };
        List<String> matches = db.relatedObjects(new TextDocument(), testStr, fieldNames, 5, null);

This can be useful, for instance, to assist users in avoiding providing duplicate content.
