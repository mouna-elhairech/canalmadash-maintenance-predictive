import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { DemandeMaintenanceService } from './demande-maintenance';

describe('DemandeMaintenanceService', () => {
  let service: DemandeMaintenanceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(DemandeMaintenanceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
