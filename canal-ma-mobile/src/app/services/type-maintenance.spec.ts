import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { TypeMaintenanceService } from './type-maintenance';

describe('TypeMaintenanceService', () => {
  let service: TypeMaintenanceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(TypeMaintenanceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
