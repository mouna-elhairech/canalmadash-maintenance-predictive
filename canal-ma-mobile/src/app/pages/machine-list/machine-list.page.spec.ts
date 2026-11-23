import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MachineListPage } from './machine-list.page';

describe('MachineListPage', () => {
  let component: MachineListPage;
  let fixture: ComponentFixture<MachineListPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(MachineListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
