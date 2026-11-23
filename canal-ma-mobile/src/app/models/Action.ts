import {TypeAction} from "./TypeAction";

export interface Action {
  id: number;
  typeAction: TypeAction;
  dateDebut: string;
  dateFin: string | null;
}
