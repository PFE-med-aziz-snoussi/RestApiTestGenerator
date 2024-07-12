import { Version } from "./version.model";

export class Change {
    id?: number;
    version?: Version;
    path?: string;
    method?: string;
    summary?: string;
    changeType?: string;
  }