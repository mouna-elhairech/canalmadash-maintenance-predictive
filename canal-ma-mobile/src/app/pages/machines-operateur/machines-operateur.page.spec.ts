import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MachinesOperateurPage } from './machines-operateur.page';

describe('MachinesOperateurPage', () => {
  let component: MachinesOperateurPage;
  let fixture: ComponentFixture<MachinesOperateurPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(MachinesOperateurPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
