import { TestBed } from '@angular/core/testing';

import { DemandeMaintenanceService } from './demande-maintenance.service';

describe('DemandeMaintenanceService', () => {
  let service: DemandeMaintenanceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DemandeMaintenanceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
