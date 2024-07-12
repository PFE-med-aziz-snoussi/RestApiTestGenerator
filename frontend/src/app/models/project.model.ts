import { User } from "./user.model";
import { Version } from "./version.model";

export class Project {
    id?: number;
    nomDuProjet: string;
    description: string;
    dateDeCreation?: Date;
    user?: User;
    versions?: Version[]; 
  }
  