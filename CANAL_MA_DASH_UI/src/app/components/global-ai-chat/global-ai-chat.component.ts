import { Component } from '@angular/core';
import { AiMaintenanceService } from '../../services/ai-maintenance.service';

interface GlobalAiChatMessage {
  role: 'USER' | 'AI';
  content: string;
  sections?: Array<{ title: string; content: string }>;
}

@Component({
  selector: 'app-global-ai-chat',
  templateUrl: './global-ai-chat.component.html'
})
export class GlobalAiChatComponent {
  open = false;
  input = '';
  loading = false;
  messages: GlobalAiChatMessage[] = [
    {
      role: 'AI',
      content: 'Bonjour. Je peux t aider a utiliser l application ou analyser les machines, pieces, demandes et interventions disponibles.'
    }
  ];

  constructor(private aiMaintenanceService: AiMaintenanceService) {}

  toggle(): void {
    this.open = !this.open;
  }

  send(): void {
    if (!this.input.trim() || this.loading) return;
    const message = this.input.trim();
    this.messages.push({ role: 'USER', content: message });
    this.input = '';
    this.loading = true;

    this.aiMaintenanceService.chat({ message }).subscribe({
      next: response => {
        this.messages.push({
          role: 'AI',
          content: response.diagnostic,
          sections: response.sections || []
        });
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.loading = false;
        this.messages.push({
          role: 'AI',
          content: 'Je ne peux pas repondre pour le moment. Verifie que le backend, Ollama et le modele sont demarres.'
        });
      }
    });
  }
}
