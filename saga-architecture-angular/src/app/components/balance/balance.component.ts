import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';

@Component({
    selector: 'app-balance',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="balance-card" [class.loading]="apiService.loading()">
      <div class="balance-header">
        <h3>💰 Saldo del Usuario</h3>
        <button class="refresh-btn" (click)="refresh()" [disabled]="apiService.loading()">
          <span class="icon" [class.spinning]="apiService.loading()">🔄</span>
        </button>
      </div>
      
      @if (apiService.error()) {
        <div class="error-message">
          ⚠️ {{ apiService.error() }}
        </div>
      }
      
      @if (apiService.balance(); as balance) {
        <div class="balance-content">
          <div class="user-id">Usuario #{{ balance.userId }}</div>
          <div class="balance-amount" [class.low-balance]="balance.balance < 100">
            {{ balance.balance | currency:'EUR':'symbol':'1.2-2' }}
          </div>
          <div class="balance-status">
            @if (balance.balance >= 100) {
              <span class="status-ok">✅ Saldo suficiente</span>
            } @else {
              <span class="status-low">⚠️ Saldo bajo</span>
            }
          </div>
        </div>
      } @else if (!apiService.error()) {
        <div class="loading-placeholder">
          <div class="skeleton"></div>
        </div>
      }
      
      <div class="auto-refresh-indicator">
        <span class="pulse"></span>
        Actualizando automáticamente
      </div>
    </div>
  `,
    styles: [`
    .balance-card {
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      border-radius: 20px;
      padding: 24px;
      color: white;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
    }
    
    .balance-card.loading {
      opacity: 0.8;
    }
    
    .balance-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .balance-header h3 {
      margin: 0;
      font-size: 1.2rem;
      font-weight: 600;
    }
    
    .refresh-btn {
      background: rgba(255, 255, 255, 0.1);
      border: none;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .refresh-btn:hover:not(:disabled) {
      background: rgba(255, 255, 255, 0.2);
      transform: scale(1.1);
    }
    
    .refresh-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    .icon {
      font-size: 1.2rem;
      display: inline-block;
    }
    
    .icon.spinning {
      animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
    
    .balance-content {
      text-align: center;
    }
    
    .user-id {
      font-size: 0.9rem;
      color: rgba(255, 255, 255, 0.6);
      margin-bottom: 8px;
    }
    
    .balance-amount {
      font-size: 3rem;
      font-weight: 700;
      background: linear-gradient(90deg, #00d9ff, #00ff88);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: 12px;
      transition: all 0.3s ease;
    }
    
    .balance-amount.low-balance {
      background: linear-gradient(90deg, #ff6b6b, #ffa502);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .balance-status {
      font-size: 0.9rem;
    }
    
    .status-ok {
      color: #00ff88;
    }
    
    .status-low {
      color: #ffa502;
    }
    
    .error-message {
      background: rgba(255, 107, 107, 0.2);
      border: 1px solid rgba(255, 107, 107, 0.5);
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 16px;
      text-align: center;
    }
    
    .loading-placeholder {
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .skeleton {
      width: 200px;
      height: 48px;
      background: linear-gradient(90deg, rgba(255,255,255,0.1) 25%, rgba(255,255,255,0.2) 50%, rgba(255,255,255,0.1) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: 8px;
    }
    
    @keyframes shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
    
    .auto-refresh-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.75rem;
      color: rgba(255, 255, 255, 0.4);
      margin-top: 20px;
      justify-content: center;
    }
    
    .pulse {
      width: 8px;
      height: 8px;
      background: #00ff88;
      border-radius: 50%;
      animation: pulse 2s infinite;
    }
    
    @keyframes pulse {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.5; transform: scale(0.8); }
    }
  `]
})
export class BalanceComponent implements OnInit {
    apiService = inject(ApiService);

    ngOnInit() {
        // Inicia el polling del saldo para el usuario 1
        this.apiService.startBalancePolling(1, 2000);
    }

    refresh() {
        this.apiService.refreshBalance();
    }
}
