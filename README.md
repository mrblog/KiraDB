# _KiraDB_

_Description: KiraDB is a simple, lightweight NoSQL-style embedded database Java API_

Storing data with KiraDB is built around `Record` interface. Each `Record` class describes  the data model, as a POJO (Plain old Java Object) implementing the `Record` interface.  

For example, let's say you're tracking high scores for a game. A `GameScore` class as follows implements the data model.

```

	public class GameScore implements Record {
	
		private static final String RECORD_NAME = "scores";
		private static final String PRIMARY_KEY = "playerId";
		private static final String SCORE = "score";
		
		private String playerId;
		private int score;
		private String playerName;
		private Boolean cheatMode;
		
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
	
		public void setCheatMode(Boolean cheatMode) {
			this.cheatMode = cheatMode;
		}
	
		public Boolean getCheatMode() {
			return cheatMode;
		}
	
		@Override
		public RecordDescriptor descriptor() {
			RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
			dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, getPlayerId()));
			dr.addField(new Field(SCORE, FieldType.NUMBER, getScore()));
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
In order to use this class with KiraDB, it would be augmented as follows to implement the `Record` interface:

```

	public class GameScore implements Record {
	
		...
	
		@Override
		public RecordDescriptor descriptor() {
			RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
			dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, getPlayerId()));
			dr.addField(new Field(SCORE, FieldType.NUMBER, getScore()));
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