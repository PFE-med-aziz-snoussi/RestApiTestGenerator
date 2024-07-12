import { Change } from "./change.model";
import { Execution } from "./execution.model";
import { Project } from "./project.model";

export class Version {
    id?: number;
    fichierOpenAPI?: string;
    fichierPostmanCollection?: string;
    project?: Project; 
    changes?: Change[]; 
    executions?: Execution[];
    createdAt?: string;
    updatedAt?: string;
  }