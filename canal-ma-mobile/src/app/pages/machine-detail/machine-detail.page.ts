import { Component, OnInit } from '@angular/core';
import {IonicModule} from "@ionic/angular";

@Component({
  selector: 'app-machine-detail',
  templateUrl: './machine-detail.page.html',
  styleUrls: ['./machine-detail.page.scss'],
  imports: [
    IonicModule
  ],
  standalone: true
})
export class MachineDetailPage implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
