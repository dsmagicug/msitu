import {
  enablePromise,
  openDatabase,
  SQLiteDatabase,
} from 'react-native-sqlite-storage';

enablePromise(true);

class ProjectService {
  public static db: SQLiteDatabase;

  private static getDBConnection(): Promise<SQLiteDatabase> {
    return openDatabase({ name: 'project.db', location: 'default' });
  }

  public static async init() {
    ProjectService.db = await ProjectService.getDBConnection();
    await ProjectService.createTables();
  }

  public static async createTables() {
    const tableQueries = [
      'CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY, name TEXT NOT NULL, basePoints TEXT NOT NULL, center TEXT,  plantingLines TEXT, markedPoints TEXT, lastLineIndex INTEGER);',
    ];

    tableQueries.map(async query => {
      await ProjectService.db.executeSql(query);
    });
  }

  public static async save(tableName: string, items: Array<any>): Promise<any> {
    if (items.length < 1) {
      return Promise.reject('Items list cannot be empty');
    }

    try {
      const firstItem = items[0];
      const fields = Object.keys(firstItem);
      const insertQuery =
        `INSERT
      OR REPLACE INTO
      ${tableName}
      (
      ${fields.join(', ')}
      )
      VALUES ` +
        items
          .map(
            i =>
              `(${fields
                .map(field =>
                  typeof i[field] === 'string' ? "'" + i[field] + "'" : i[field],
                )
                .join(', ')})`,
          )
          .join(',');

      return ProjectService.db.executeSql(insertQuery);
    } catch (error) {
      return Promise.reject(error)
    }
  }

  public static count(tableName: string): Promise<number> {
    return new Promise<number>((resolve, reject) => {
      ProjectService.db.transaction(tx => {
        tx.executeSql(
          `SELECT COUNT(*) as count FROM ${tableName}`,
          [],
          (_, { rows }) => {
            const count = rows.item(0).count;
            resolve(count);
          },
          (_, error) => {
            reject(error);
          },
        );
      });
    });
  }

  public static async update(
    tableName: string,
    id: number,
    updateData: {
      [key: string]: number | string,
    },
  ): Promise<any> {
    try {
      const fields: string[] = Object.keys(updateData);
      const updateStatements = fields.map(field => {
        if (typeof updateData[field] === 'string') {
          return `${field}='${updateData[field]}'`;
        } else {
          return `${field}=${updateData[field]}`;
        }
      });
      const query = `UPDATE ${tableName} SET ${updateStatements.join(
        ', ',
      )} WHERE id=${id}`;

      return new Promise((resolve, reject) => {
        ProjectService.db.transaction(tx => {
          tx.executeSql(
            query,
            [],
            (_, result) => {
              if (result.rowsAffected > 0) {
                tx.executeSql(
                  `SELECT * FROM ${tableName} WHERE id = last_insert_rowid()`,
                  [],
                  (_, { rows }) => {
                    resolve(rows.item(0));
                  },
                  (_, error) => {
                    reject(error);
                  },
                );
              } else {
                reject(new Error('No rows affected'));
              }
            },
            (_, error) => {
              reject(error);
            },
          );
        });
      });
    } catch (error) {
      return Promise.reject(error);
    }
  }

  public static async fetch(
    tableName: String,
    lookups: {
      [key: string]: number | string;
    } | null = null,
    limit: number | 10 = 10,
    orderBy: string | 'id' = 'id',
  ): Promise<any> {
    try {
      const items: any[] = [];
      const query = lookups
        ? `SELECT * FROM ${tableName} WHERE (${Object.keys(lookups)
          .map(
            key =>
              `${key}=${typeof lookups[key] === 'string'
                ? "'" + lookups[key] + "'"
                : lookups[key]
              }`,
          )
          .join(' AND ')}) ORDER BY ${orderBy} DESC LIMIT ${limit}`
        : `SELECT * FROM ${tableName} ORDER BY ${orderBy} DESC LIMIT ${limit}`;
      const results = await ProjectService.db.executeSql(query);
      results.forEach(result => {
        for (let index = 0; index < result.rows.length; index++) {
          items.push(result.rows.item(index));
        }
      });
      return items;
    } catch (error) {
      return Promise.reject(error);
    }
  }

  public static async getById(
    tableName: String,
    id: number | undefined,
  ): Promise<any> {
    try {
      let item: any = null;
      const query = `SELECT * FROM ${tableName} WHERE id= ?`;
      const results = await ProjectService.db.executeSql(query, [id]);
      results.forEach(result => {
        if (result.rows.length > 0) {
          item = result.rows.item(0);
        }
      });
      return item;
    } catch (error) {
      return Promise.reject(error);
    }
  }

  public static async deleteItem(id: number, tableName: string): Promise<any> {
    try {
      const deleteQuery = `DELETE from ${tableName} where id = ${id}`;
      await ProjectService.db.executeSql(deleteQuery);
      return Promise.resolve(false);
    } catch (e) {
      return Promise.reject(e);
    }
  }
}

export default ProjectService;
