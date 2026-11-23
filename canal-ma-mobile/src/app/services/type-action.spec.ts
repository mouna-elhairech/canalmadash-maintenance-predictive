import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { TypeActionService } from './type-action';

describe('TypeActionService', () => {
  let service: TypeActionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(TypeActionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
