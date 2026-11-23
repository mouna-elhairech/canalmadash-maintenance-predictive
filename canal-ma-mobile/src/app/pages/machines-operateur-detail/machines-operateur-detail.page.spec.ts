import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MachinesOperateurDetailPage } from './machines-operateur-detail.page';

describe('MachinesOperateurDetailPage', () => {
  let component: MachinesOperateurDetailPage;
  let fixture: ComponentFixture<MachinesOperateurDetailPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(MachinesOperateurDetailPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
