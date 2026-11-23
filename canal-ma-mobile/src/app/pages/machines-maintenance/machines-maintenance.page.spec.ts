import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MachinesMaintenancePage } from './machines-maintenance.page';

describe('MachinesMaintenancePage', () => {
  let component: MachinesMaintenancePage;
  let fixture: ComponentFixture<MachinesMaintenancePage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(MachinesMaintenancePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
