import { TestBed } from '@angular/core/testing';

import { TypeActionService } from './type-action.service';

describe('TypeActionService', () => {
  let service: TypeActionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TypeActionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
