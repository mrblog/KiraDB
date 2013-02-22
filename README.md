# _KiraDB_

_Description: KiraDB is a simple, lightweight NoSQL-style embedded database Java API_

Storing data with KiraDB is built around `Record` interface. Each `Record` class describes  the data model, as a POJO (Plain old Java Object) implementing the `Record` interface.  

For example, let's say you're tracking high scores for a game. A `GameScore` class as follows implements the data model.

```

	public class GameScore implements Record {
	
		private String playerId;
		private int score;
		private String playerName;
		private String team;
		private Boolean cheatMode;
		
		public GameScore() {
		}

		public GameScore(String playerId, int score, String playerName, Boolean cheatMode) {
			setPlayerId(playerId);
			setScore(score);
			setPlayerName(playerName);
			setCheatMode(cheatMode);
		}
		public void setPlayerId(String playerId) {
			this.playerId = playerId;
		}
	
		public String getPlayerId() {
			return playerId;
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
		private static final String PRIMARY_KEY = "playerId";
		public static final String SCORE = "score";
		public static final String TEAM = "team";

		...
	
		@Override
		public RecordDescriptor descriptor() {
			RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
			dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, getPlayerId()));
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

Each `Record` is an instance of a specific subclass with a class name and record name  that you can use to distinguish different sorts of data. For example, we call the high score object a `GameScore` class with the record name `"scores"` (`RECORD_NAME`). We recommend that you NameYourClassesLikeThis and nameYourKeysLikeThis, just to keep your code looking pretty.

To create a new subclass, implement the `Record` interface. KiraDB will return instances of the new class for any Object with the specified record name

## Initializing KiraDB

```

	KiraDb db = new KiraDb(new File("KiraDBIndex"));

```

You also need to initialize the index (one-time).

```

	db.createIndex();
```



## Saving Objects

Let's say you want to save the GameScore described above to KiraDB. 

```

	GameScore p1 = new GameScore("kevin", 1337, "Kevin Blake", false);
	db.storeObject(p1);
```


## Retrieving Objects

If you have the primary key value, you can retrieve the Object using the `retrieveObjectByPrimaryKey` method:

```

	GameScore theScore = (GameScore) db.retrieveObjectByPrimaryKey("kevin");

```

## Queries

We've already seen how you can retrieve a single Object from KiraDB. There are many other ways to retrieve data with the `executeQuery` method - you can retrieve many objects at once, put conditions on the objects you wish to retrieve, and more.

### Basic Queries

The general approach is to create a query, put conditions on it, and then retrieve a `List` of matching Objects using `executeQuery`. For example, to retrieve of the scores associated with with a particular team, by constraining the value for a key.

```

        List<Object> qResults = db.executeQuery(new GameScore(), GameScore.TEAM, "shaggy", 10, 0, GameScore.SCORE, true);


```

This returns scores belonging to team "shaggy" returning 10 results at a time, skipping 0 results (i.e. starting at 0), sorting by SCORE in reverse sort order (highest score first).



### Primary Keys

Primary Keys in KiraDB must be `String` fields (`FieldType.STRING`) which are simple case-sensitive string values. The Primary Key is unique across all records of this class.

### Fields

The `GameScore` example adds a field for the user's score. This field is to be treated as a `NUMBER` by KiraDB. This effects searching and sorting based on the field.

### Object Store

KiraDB can operate in multiple modes:

1. Indexing (supporting searching and sorting operations) only (no storage of the full object)
2. Storing Objects in the KiraDB index
3. Storing Objects in a separate Backing Store (`BackingStore`)

KiraDB includes two included backing store implementations, a simple File-based object store (`FileBackingStore`) and a Amazon S3 based backing store (`S3BackingStore`)

Users can provide their own custom backing store by implementing their own `BackingStore` class.

### Record, Key, and Field Names

Record, Primary Key, and Field Names are case-sensitive and must consist of letters and digits only.
