import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, OrderRequest } from '../../services/api.service';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="order-card">
      <div class="order-header">
        <h3>🛒 Crear Pedido</h3>
      </div>
      
      <form (ngSubmit)="submitOrder()" class="order-form">
        <div class="form-group">
          <label for="userId">ID de Usuario</label>
          <input 
            type="number" 
            id="userId" 
            [(ngModel)]="order.userId" 
            name="userId"
            min="1"
            required
          >
        </div>
        
        <div class="form-group">
          <label for="productId">ID de Producto</label>
          <input 
            type="number" 
            id="productId" 
            [(ngModel)]="order.productId" 
            name="productId"
            min="1"
            required
          >
          <span class="hint">Productos disponibles: 101 (stock), 102 (sin stock)</span>
        </div>
        
        <div class="form-group">
          <label for="amount">Monto (€)</label>
          <input 
            type="number" 
            id="amount" 
            [(ngModel)]="order.amount" 
            name="amount"
            min="0"
            step="0.01"
            required
          >
        </div>
        
        <button 
          type="submit" 
          class="submit-btn" 
          [disabled]="isSubmitting()"
        >
          @if (isSubmitting()) {
            <span class="spinner"></span>
            Procesando...
          } @else {
            Crear Pedido
          }
        </button>
      </form>
      
      @if (message()) {
        <div class="message" [class.success]="isSuccess()" [class.error]="!isSuccess()">
          {{ message() }}
        </div>
      }
      
      @if (lastOrder()) {
        <div class="last-order">
          <h4>📦 Último Pedido</h4>
          <div class="order-details">
            <div class="detail-row">
              <span class="label">ID:</span>
              <span class="value">{{ lastOrder()?.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Estado:</span>
              <span class="value status" [class]="lastOrder()?.orderStatus?.toLowerCase()">
                {{ lastOrder()?.orderStatus }}
              </span>
            </div>
            <div class="detail-row">
              <span class="label">Monto:</span>
              <span class="value">{{ lastOrder()?.price | currency:'EUR' }}</span>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .order-card {
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
      border-radius: 20px;
      padding: 24px;
      color: white;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    
    .order-header h3 {
      margin: 0 0 24px 0;
      font-size: 1.2rem;
      font-weight: 600;
    }
    
    .order-form {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .form-group label {
      font-size: 0.9rem;
      color: rgba(255, 255, 255, 0.7);
    }
    
    .form-group input {
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      border-radius: 10px;
      padding: 14px 16px;
      color: white;
      font-size: 1rem;
      transition: all 0.3s ease;
    }
    
    .form-group input:focus {
      outline: none;
      border-color: #00d9ff;
      background: rgba(255, 255, 255, 0.15);
      box-shadow: 0 0 0 3px rgba(0, 217, 255, 0.2);
    }
    
    .form-group input::placeholder {
      color: rgba(255, 255, 255, 0.4);
    }
    
    .hint {
      font-size: 0.75rem;
      color: rgba(255, 255, 255, 0.4);
    }
    
    .submit-btn {
      background: linear-gradient(90deg, #00d9ff, #00ff88);
      border: none;
      border-radius: 10px;
      padding: 16px;
      color: #1a1a2e;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
    
    .submit-btn:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 10px 30px rgba(0, 217, 255, 0.4);
    }
    
    .submit-btn:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    
    .spinner {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(26, 26, 46, 0.3);
      border-top-color: #1a1a2e;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    
    .message {
      margin-top: 16px;
      padding: 12px 16px;
      border-radius: 10px;
      text-align: center;
      font-size: 0.9rem;
    }
    
    .message.success {
      background: rgba(0, 255, 136, 0.2);
      border: 1px solid rgba(0, 255, 136, 0.5);
      color: #00ff88;
    }
    
    .message.error {
      background: rgba(255, 107, 107, 0.2);
      border: 1px solid rgba(255, 107, 107, 0.5);
      color: #ff6b6b;
    }
    
    .last-order {
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
    }
    
    .last-order h4 {
      margin: 0 0 16px 0;
      font-size: 1rem;
      font-weight: 500;
    }
    
    .order-details {
      background: rgba(255, 255, 255, 0.05);
      border-radius: 10px;
      padding: 16px;
    }
    
    .detail-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }
    
    .detail-row:last-child {
      border-bottom: none;
    }
    
    .label {
      color: rgba(255, 255, 255, 0.5);
    }
    
    .value {
      font-weight: 500;
    }
    
    .status {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 0.8rem;
    }
    
    .status.order_created, .status.created {
      background: rgba(0, 217, 255, 0.2);
      color: #00d9ff;
    }
    
    .status.completed {
      background: rgba(0, 255, 136, 0.2);
      color: #00ff88;
    }
    
    .status.cancelled, .status.failed {
      background: rgba(255, 107, 107, 0.2);
      color: #ff6b6b;
    }
  `]
})
export class OrderFormComponent {
  private apiService = inject(ApiService);

  order: OrderRequest = {
    userId: 1,
    productId: 101,
    amount: 100.0
  };

  isSubmitting = signal(false);
  message = signal<string | null>(null);
  isSuccess = signal(false);
  lastOrder = signal<any>(null);

  async submitOrder() {
    this.isSubmitting.set(true);
    this.message.set(null);

    try {
      const response = await this.apiService.createOrder(this.order);
      this.lastOrder.set(response);
      this.isSuccess.set(true);
      this.message.set('✅ Pedido creado exitosamente');
    } catch (error: any) {
      this.isSuccess.set(false);
      this.message.set('❌ Error al crear el pedido: ' + (error.message || 'Error desconocido'));
    } finally {
      this.isSubmitting.set(false);
    }
  }
}
