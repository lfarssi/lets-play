const dbName = process.env.MONGO_INITDB_DATABASE || "lets_play";
const appUser = process.env.MONGO_APP_USERNAME || "letsplay";
const appPass = process.env.MONGO_APP_PASSWORD || "letsplaypass";

db = db.getSiblingDB(dbName);

db.createUser({
  user: appUser,
  pwd: appPass,
  roles: [{ role: "readWrite", db: dbName }]
});

print(`✅ Created app user '${appUser}' on '${dbName}'`);
