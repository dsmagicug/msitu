import {
  enablePromise,
  openDatabase,
  SQLiteDatabase,
} from 'react-native-sqlite-storage';
import { Project } from '../models';

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
      'CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY, name TEXT NOT NULL, basePoints TEXT NOT NULL, center TEXT,  plantingLines TEXT, markedPoints TEXT, gapSize INTEGER, lineLength INTEGER, gapSizeUnit TEXT, lineLengthUnit TEXT, forwardIndex INTEGER, backwardIndex INTEGER,lineCount INTEGER, createdAt TEXT);',
      'CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY, appMode TEXT, skipLines INTEGER, displayLineCount INTEGER, cloudApi TEXT, mapStyle TEXT, highContrastMode INTEGER DEFAULT 0);',
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

      // Use parameterized query to prevent data logging
      const placeholders = items.map(() => `(${fields.map(() => '?').join(', ')})`).join(',');
      const insertQuery = `INSERT OR REPLACE INTO ${tableName} (${fields.join(', ')}) VALUES ${placeholders}`;

      // Flatten the values array for parameterized query
      const values = items.flatMap(item => fields.map(field => item[field]));

      // Execute with parameterized query to prevent data exposure
     return await ProjectService.db.executeSql(insertQuery, values);
    } catch (error) {
      return Promise.reject(error);
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
                resolve(result);
                /* tx.executeSql(
                  `SELECT * FROM ${tableName} WHERE id = ${id}`,
                  [],
                  (_, { rows }) => {
                    resolve(rows.item(0));
                  },
                  (_, error) => {
                    reject(error);
                  },
                ); */
              } else {
                reject(new Error('No rows affected'));
              }
            },
            (_, error) => {
              console.error(error);
              reject(error);
            },
          );
        });
      });
    } catch (error:any) {
      console.error(error);
      return Promise.reject(error.message);
    }
  }

  public static async fetch(
    tableName: string,
    fields: any = '*',
    lookups: { [key: string]: number | string } | null = null,
    limit: number = 10,
    orderBy: string = 'id',
  ): Promise<any> {
    try {
      const items: any[] = [];

      const selectedFields = typeof fields === 'string' && fields === '*'
      ? '*'
      : Array.isArray(fields)
      ? fields.join(', ')
      : '*';

      const query = lookups
        ? `SELECT ${selectedFields} FROM ${tableName} WHERE (${Object.keys(lookups)
            .map(
              key =>
                `${key}=${typeof lookups[key] === 'string'
                  ? "'" + lookups[key] + "'"
                  : lookups[key]
                }`,
            )
            .join(' AND ')}) ORDER BY ${orderBy} DESC LIMIT ${limit}`
        : `SELECT ${selectedFields} FROM ${tableName} ORDER BY ${orderBy} DESC LIMIT ${limit}`;

      const results = await ProjectService.db.executeSql(query);
      results.forEach(result => {
        for (let index = 0; index < result.rows.length; index++) {
          items.push(result.rows.item(index));
        }
      });
      return items;
    } catch (error:any) {
      console.error(error);
      return Promise.reject(error.message);
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
      console.error(error);
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

  public static async exportProject(projectId: number): Promise<Project> {
    try {
      // Get project from database
      const projectData = await ProjectService.getById('projects', projectId);

      if (!projectData) {
        throw new Error('Project not found');
      }

      // Parse JSON fields
      const basePoints = projectData.basePoints ? JSON.parse(projectData.basePoints) : [];
      const center = projectData.center ? JSON.parse(projectData.center) : null;
      const plantingLines = projectData.plantingLines ? JSON.parse(projectData.plantingLines) : [];
      const markedPoints = projectData.markedPoints ? JSON.parse(projectData.markedPoints) : [];
      const createdAt = projectData.createdAt ? new Date(projectData.createdAt) : null;

      // Parse Project object with proper types from text
      const project: Project = {
        id: projectData.id,
        name: projectData.name,
        basePoints: basePoints,
        gapSize: projectData.gapSize,
        lineLength: projectData.lineLength,
        gapSizeUnit: projectData.gapSizeUnit,
        lineLengthUnit: projectData.lineLengthUnit,
        createdAt: createdAt,
        center: center,
        plantingLines: plantingLines,
        markedPoints: markedPoints,
        forwardIndex: projectData.forwardIndex || 9,
        backwardIndex: projectData.backwardIndex || 0,
        lineCount: projectData.lineCount,
      };

      return project;
    } catch (error) {
      console.error('Failed to export project:', error);
      return Promise.reject(error);
    }
  }
}

export default ProjectService;
