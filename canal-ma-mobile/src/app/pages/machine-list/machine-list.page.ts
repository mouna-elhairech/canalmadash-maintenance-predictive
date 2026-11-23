import { Component, OnInit } from '@angular/core';
import {IonicModule} from "@ionic/angular";

@Component({
  selector: 'app-machine-list',
  templateUrl: './machine-list.page.html',
  styleUrls: ['./machine-list.page.scss'],
  imports: [
    IonicModule
  ],
  standalone: true
})
export class MachineListPage implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
