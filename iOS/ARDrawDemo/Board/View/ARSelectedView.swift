//
//  ARSelectedView.swift
//  ARDrawDemo
//
//  Created by zjq on 2022/1/19.
//

import UIKit

enum ARSelectedItemMode: Int {
    case BlueItemMode
    case GreenItemMode
    case RedItemMode
    case PanItemMode
}

typealias sliderBlock = (_ value: Int, _ mode: ARSelectedItemMode ) -> Void

// 单个选项
class ARSelectedItemView: UIView {
    
    // 闭包传递sliderValue
    var sliderChange: sliderBlock?
    
    var titleLabel: UILabel!
    var slider: UISlider!
    var valueLabel: UILabel!
    var itemMode: ARSelectedItemMode? {
        didSet {
            switch itemMode {
                case .some(.BlueItemMode):
                    titleLabel.text = "蓝"
                    slider.maximumValue = 255
                    slider.value = 24
                    valueLabel.text = "24"
                    break
                case .some(.GreenItemMode):
                    titleLabel.text = "绿"
                    slider.maximumValue = 255
                    slider.value = 24
                    valueLabel.text = "24"
                    break
                case .some(.RedItemMode):
                    titleLabel.text = "红"
                    slider.maximumValue = 255
                    slider.value = 24
                    valueLabel.text = "24"
                    break
                case .some(.PanItemMode):
                    titleLabel.text = "粗细"
                    slider.minimumValue = 1
                    slider.maximumValue = 10
                    slider.value = 2
                    valueLabel.text = "24"
                    break
                default:
                    break
            }
        }
    }
    //code
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupSubviews()
    }
    //xib
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupSubviews()
    }
    
    func setupSubviews() {
       
        self.backgroundColor = UIColor.clear
        // 前标题
        titleLabel = UILabel()
        titleLabel.textAlignment = .center
        titleLabel.font = UIFont.systemFont(ofSize: 12)
        titleLabel.textColor = UIColor.black
        self.addSubview(titleLabel)
        // 滑块
        slider = UISlider()
        slider.minimumValue = 0
        slider.addTarget(self, action: #selector(sliderValue(sender:)), for: .valueChanged)
        self.addSubview(slider)
        // 后标题
        valueLabel = UILabel()
        valueLabel.font = UIFont.systemFont(ofSize: 12)
        valueLabel.textAlignment = .center
        valueLabel.textColor = UIColor.black
        self.addSubview(valueLabel)
        
        titleLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.left.equalToSuperview().offset(10)
            make.right.equalTo(slider.snp_left).offset(-10)
//            make.width.equalTo(30)
        }
    
        slider.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.right.equalTo(valueLabel.snp.left).offset(-10)
            make.height.equalTo(20)
            make.left.equalTo(titleLabel.snp_right).offset(10)
        }
        valueLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.right.equalToSuperview().offset(-10)
          //  make.left.equalTo(slider.snp_right).offset(10)
            make.width.equalTo(30)
        }
    }
    
    @objc func sliderValue(sender: UISlider) {
        let sliderValue:Float = sender.value
        self.valueLabel.text = String(format: "%d", Int(sliderValue))
        if sliderChange != nil {
            sliderChange!(Int(sliderValue),self.itemMode!)
        }
    }
}

// 类型
enum ARSelectedMode: Int {
    case ColorMode
    case PanModel
}

typealias selectedColorBlock = (_ value: UIColor) -> Void
typealias selectedPanBlock = (_ value: Int) -> Void

class ARSelectedView: UIView {
    // 颜色
    var color: UIColor! {
        didSet {
            let blueItem: ARSelectedItemView = self.viewWithTag(100) as! ARSelectedItemView
            let greenItem: ARSelectedItemView = self.viewWithTag(101) as! ARSelectedItemView
            let redItem: ARSelectedItemView = self.viewWithTag(102) as! ARSelectedItemView
            
            var redValue: CGFloat = 0.0
            var greenValue: CGFloat = 0.0
            var blueValue :CGFloat = 0.0
            var alpha :CGFloat = 0.0
            color.getRed(&redValue, green: &greenValue, blue: &blueValue, alpha: &alpha)
            
            blueValue = blueValue * 255
            blueItem.slider.value = Float(blueValue)
            blueItem.valueLabel.text = String(format:"%.0f",blueValue)
            self.blue = Int(blueValue)
            
            greenValue = greenValue * 255
            greenItem.slider.value = Float(greenValue)
            greenItem.valueLabel.text = String(format:"%.0f",greenValue)
            self.green = Int(greenValue)
            
            redValue = redValue * 255
            redItem.slider.value = Float(redValue)
            redItem.valueLabel.text = String(format:"%.0f",redValue)
            self.red = Int(redValue)
        }
    }
    // 闭包传递
    var colorBlock: selectedColorBlock?
    var panBlock: selectedPanBlock?
    
    fileprivate var blue: Int!
    fileprivate var green: Int!
    fileprivate var red: Int!
    
    // 粗细
    var line: Int? {
        didSet {
            let item: ARSelectedItemView = self.viewWithTag(1000) as! ARSelectedItemView
            item.slider.value = Float(line!)
            item.valueLabel.text = String(format:"%d",line!)
        }
    }
    
    
    // 类型
    var mode: ARSelectedMode? {
        didSet {
            setupSubviews()
        }
    }
    // 布局
    func setupSubviews() {
        self.backgroundColor = UIColor.clear
        self.alpha = 0.7
        // stackView
        let stackView: UIStackView = UIStackView()
        stackView.axis = .vertical
        stackView.distribution = .fillEqually
        stackView.spacing = 0
        stackView.alignment = .fill
        self.addSubview(stackView)
        stackView.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0))
        }
        if  mode == .ColorMode {
            // 布局
            for i in 0..<3 {
                let item :ARSelectedItemView = ARSelectedItemView()
                item.tag = 100 + i
                switch i {
                    case 0:
                        item.itemMode = .BlueItemMode
                        break
                    case 1:
                        item.itemMode = .GreenItemMode
                        break
                    case 2:
                        item.itemMode = .RedItemMode
                        break
                    default:
                        break
                }
                // 值的修改
                item.sliderChange = { (value: Int, mode: ARSelectedItemMode) -> Void in
                    if mode == .BlueItemMode {
                        self.blue = value
                    }else if mode == .GreenItemMode {
                        self.green = value
                    }else if mode == .RedItemMode {
                        self.red = value
                    }
                    let color: UIColor = UIColor.init(red: CGFloat(self.red)/255.0, green: CGFloat(self.green)/255.0, blue: CGFloat(self.blue)/255.0, alpha: 1.0)
                   
                    if self.colorBlock != nil {
                        self.colorBlock!(color)
                    }
                }
                stackView.addArrangedSubview(item)
                stackView.layoutIfNeeded()
            }
        }else{
            let item :ARSelectedItemView = ARSelectedItemView()
            item.tag = 1000
            item.itemMode = .PanItemMode
            item.sliderChange = { (value: Int, mode: ARSelectedItemMode) -> Void in
                if self.panBlock != nil {
                    self.panBlock!(value)
                }
            }
            stackView.addArrangedSubview(item)
            stackView.layoutIfNeeded()
        }
    }
    
    
    
    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */

}
