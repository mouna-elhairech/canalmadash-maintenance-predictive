import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TypeParametresComponent } from './type-parametres.component';

describe('TypeParametresComponent', () => {
  let component: TypeParametresComponent;
  let fixture: ComponentFixture<TypeParametresComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TypeParametresComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TypeParametresComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
