import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-saga-log',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="log-card">
      <div class="log-header">
        <h3>📜 Registro de la Saga</h3>
      </div>
      
      <div class="log-content">
        @if (apiService.events().length === 0) {
          <div class="empty-log">
            Esperando eventos...
          </div>
        }
        
        <div class="event-list">
          @for (event of apiService.events(); track event.time) {
            <div class="event-item" [class]="event.type">
              <span class="event-time">{{ event.time | date:'HH:mm:ss' }}</span>
              <span class="event-message">{{ event.message }}</span>
            </div>
          }
        </div>
      </div>
    </div>
  `,
    styles: [`
    .log-card {
      background: rgba(0, 0, 0, 0.4);
      border-radius: 20px;
      padding: 24px;
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.1);
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    
    .log-header h3 {
      margin: 0 0 16px 0;
      font-size: 1.1rem;
      font-weight: 600;
      color: rgba(255, 255, 255, 0.8);
    }
    
    .log-content {
      flex: 1;
      overflow-y: auto;
      max-height: 400px;
    }
    
    .empty-log {
      text-align: center;
      padding: 40px;
      color: rgba(255, 255, 255, 0.3);
      font-style: italic;
    }
    
    .event-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .event-item {
      padding: 12px 16px;
      border-radius: 12px;
      font-size: 0.9rem;
      display: flex;
      gap: 12px;
      align-items: flex-start;
      border-left: 4px solid transparent;
      background: rgba(255, 255, 255, 0.03);
      animation: slideIn 0.3s ease-out;
    }
    
    @keyframes slideIn {
      from { opacity: 0; transform: translateX(-10px); }
      to { opacity: 1; transform: translateX(0); }
    }
    
    .event-time {
      font-family: monospace;
      color: rgba(255, 255, 255, 0.4);
      font-size: 0.8rem;
      white-space: nowrap;
      margin-top: 2px;
    }
    
    .event-item.info {
      border-left-color: #00d9ff;
    }
    
    .event-item.success {
      border-left-color: #00ff88;
      background: rgba(0, 255, 136, 0.05);
    }
    
    .event-item.warning {
      border-left-color: #ffa502;
      background: rgba(255, 165, 2, 0.05);
    }
    
    .event-item.error {
      border-left-color: #ff6b6b;
      background: rgba(255, 107, 107, 0.05);
    }
    
    .event-message {
      color: rgba(255, 255, 255, 0.9);
    }
  `]
})
export class SagaLogComponent {
    apiService = inject(ApiService);
}
