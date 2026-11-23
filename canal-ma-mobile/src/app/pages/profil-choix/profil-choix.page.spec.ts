import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfilChoixPage } from './profil-choix.page';

describe('ProfilChoixPage', () => {
  let component: ProfilChoixPage;
  let fixture: ComponentFixture<ProfilChoixPage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfilChoixPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
